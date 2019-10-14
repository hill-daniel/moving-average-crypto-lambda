package org.kiwi.calculation;

import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import org.kiwi.alert.DeviationAlert;
import org.kiwi.aws.metrics.CloudWatchMetricsWriter;
import org.kiwi.crypto.api.CoinMarketCap;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.DepotRepository;
import org.kiwi.proto.FloatingAverageProtos.Depot;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class FloatingAverageJob {

    private final CurrencyRepository currencyRepository;
    private final DepotRepository depotRepository;
    private final FloatingAverageCalculator floatingAverageCalculator;
    private final DeviationAlert deviationAlert;
    private final CloudWatchMetricsWriter metricsWriter;
    private final String depotId;

    @Inject
    public FloatingAverageJob(@CoinMarketCap CurrencyRepository currencyRepository,
            DepotRepository depotRepository,
            FloatingAverageCalculator floatingAverageCalculator,
            DeviationAlert deviationAlert, CloudWatchMetricsWriter metricsWriter,
            @Named("crypto.depot.id") String depotId) {
        this.currencyRepository = currencyRepository;
        this.depotRepository = depotRepository;
        this.floatingAverageCalculator = floatingAverageCalculator;
        this.deviationAlert = deviationAlert;
        this.metricsWriter = metricsWriter;
        this.depotId = depotId;
    }

    public void execute() {
        Collection<Currency> currencies = retrieveCurrencies();
        Map<String, FloatingAverage> idToAverage = loadAveragesAndGroupById();
        Collection<FloatingAverage> latestFloatingAverages = calculateLatestAverages(currencies, idToAverage);

        store(latestFloatingAverages);

        alertForChangedState(idToAverage, latestFloatingAverages, getTop100CurrencyIds(currencies));
    }

    private Collection<Currency> retrieveCurrencies() {
        return metricsWriter.executeWithMetric(
                currencyRepository::retrieveCurrencies,
                "currencyAPILoad");
    }

    private Map<String, FloatingAverage> loadAveragesAndGroupById() {
        return metricsWriter.executeWithMetric(
                () -> depotRepository.load(depotId)
                        .map(this::toIdToAverageMap)
                        .orElse(emptyMap()),
                "s3AverageLoad");
    }

    private Map<String, FloatingAverage> toIdToAverageMap(Depot depot) {
        return depot.getFloatingAveragesList().stream()
                .collect(toMap(FloatingAverage::getId, floatingAverage -> floatingAverage));
    }

    private Collection<FloatingAverage> calculateLatestAverages(Collection<Currency> currencies,
            Map<String, FloatingAverage> idToAverage) {
        return metricsWriter.executeWithMetric(() ->
                        currencies.parallelStream()
                                .map(toLatestFloatingAverage(idToAverage))
                                .collect(toSet()),
                "averageCalculation");
    }

    private Function<Currency, FloatingAverage> toLatestFloatingAverage(Map<String, FloatingAverage> idToAverage) {
        return currency -> {
            FloatingAverage floatingAverage = idToAverage.get(currency.id());
            return floatingAverageCalculator.calculate(currency, floatingAverage);
        };
    }

    private void store(Collection<FloatingAverage> latestFloatingAverages) {
        metricsWriter.executeWithMetric(() -> {
            Depot depot = Depot.newBuilder()
                    .setId(depotId)
                    .addAllFloatingAverages(latestFloatingAverages)
                    .build();
            depotRepository.store(depot);
        }, "s3AverageStore");
    }

    private void alertForChangedState(Map<String, FloatingAverage> idToAverage,
            Collection<FloatingAverage> latestFloatingAverages,
            Collection<String> top100CurrencyIds) {
        Set<FloatingAverage> averagesWithChangedState = latestFloatingAverages.stream()
                .filter(floatingAverage -> top100CurrencyIds.contains(floatingAverage.getId()))
                .filter(alertStateHasChanged(idToAverage))
                .collect(toSet());
        if (!averagesWithChangedState.isEmpty()) {
            deviationAlert.alert(averagesWithChangedState);
        }
    }

    private Set<String> getTop100CurrencyIds(Collection<Currency> currencies) {
        return currencies.stream()
                .filter(currency -> currency.rank() <= 100)
                .map(Currency::id)
                .collect(toSet());
    }

    private Predicate<FloatingAverage> alertStateHasChanged(Map<String, FloatingAverage> idToAverage) {
        return currentAverage -> {
            FloatingAverage previousAverage = idToAverage.get(currentAverage.getId());
            return previousAverage != null && previousAverage.getAlertState() != currentAverage.getAlertState();
        };
    }

}
