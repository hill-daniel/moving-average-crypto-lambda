package org.kiwi.calculation;

import static java.time.ZoneOffset.UTC;
import static java.util.Collections.singleton;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.alert.DeviationAlert;
import org.kiwi.aws.metrics.CloudWatchMetricsWriter;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.DepotRepository;
import org.kiwi.proto.FloatingAverageProtos.Depot;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.Quote;

public class FloatingAverageJobTest {

    private static final Currency BITCOIN = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
            LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC), 0);
    private static final FloatingAverage BITCOIN_AVERAGE = createBitcoinTestData();
    private static final Depot DEPOT = Depot.newBuilder()
            .setId("test-depot")
            .addFloatingAverages(BITCOIN_AVERAGE)
            .build();

    private FloatingAverageJob job;
    private DepotRepository depotRepository;
    private FloatingAverageCalculator floatingAverageCalculator;
    private DeviationAlert deviationAlert;

    @Before
    public void setUp() throws Exception {
        CurrencyRepository currencyRepository = mock(CurrencyRepository.class);
        when(currencyRepository.retrieveCurrencies()).thenReturn(singleton(BITCOIN));
        depotRepository = mock(DepotRepository.class);
        when(depotRepository.load("test-depot")).thenReturn(Optional.of(DEPOT));
        floatingAverageCalculator = mock(FloatingAverageCalculator.class);
        deviationAlert = mock(DeviationAlert.class);
        CloudWatchMetricsWriter cloudWatchMetricsWriter = new TestMetricWriter();
        job = new FloatingAverageJob(currencyRepository, depotRepository, floatingAverageCalculator,
                deviationAlert, cloudWatchMetricsWriter, "test-depot");
    }

    @Test
    public void should_calculate_and_store_latest_average() throws Exception {
        Quote newBitcoinQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage newBitcoinAverage = newBuilder(BITCOIN_AVERAGE)
                .setLatestAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(newBitcoinQuote)
                .build();
        Depot depot = Depot.newBuilder()
                .setId("test-depot")
                .addFloatingAverages(newBitcoinAverage)
                .build();

        when(floatingAverageCalculator.calculate(BITCOIN, BITCOIN_AVERAGE)).thenReturn(newBitcoinAverage);

        job.execute();

        verify(depotRepository).store(depot);
        verifyNoMoreInteractions(deviationAlert);
    }

    @Test
    public void should_notify_with_alert_when_state_has_changed() throws Exception {
        Quote newBitcoinQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage newBitcoinAverage = newBuilder(BITCOIN_AVERAGE)
                .setLatestAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(newBitcoinQuote)
                .setAlertState(AlertState.SELL)
                .build();
        Depot depot = Depot.newBuilder()
                .setId("test-depot")
                .addFloatingAverages(newBitcoinAverage)
                .build();
        when(floatingAverageCalculator.calculate(BITCOIN, BITCOIN_AVERAGE)).thenReturn(newBitcoinAverage);

        job.execute();

        verify(depotRepository).store(depot);
        verify(deviationAlert).alert(singleton(newBitcoinAverage));
    }

    @Test
    public void should_not_alert_again_on_same_state() throws Exception {
        FloatingAverage currentAverage = newBuilder(BITCOIN_AVERAGE)
                .setAlertState(AlertState.SELL).build();
        Depot depot = Depot.newBuilder()
                .setId("test-depot")
                .addFloatingAverages(currentAverage)
                .build();
        when(depotRepository.load("test-depot")).thenReturn(Optional.of(depot));
        Quote newBitcoinQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage newBitcoinAverage = newBuilder(BITCOIN_AVERAGE)
                .setLatestAverage("1213.82")
                .setClosingDate(1485129600)
                .addQuotes(newBitcoinQuote)
                .setAlertState(AlertState.SELL)
                .build();
        Depot newDepot = Depot.newBuilder()
                .setId("test-depot")
                .addFloatingAverages(newBitcoinAverage)
                .build();
        when(floatingAverageCalculator.calculate(BITCOIN, currentAverage)).thenReturn(newBitcoinAverage);

        job.execute();

        verify(depotRepository).store(newDepot);
        verifyNoMoreInteractions(deviationAlert);
    }

    @Test
    public void should_store_latest_average_if_no_historical_data_is_available() throws Exception {
        Depot emptyDepot = Depot.newBuilder()
                .setId("test-depot")
                .build();
        when(depotRepository.load("test-depot")).thenReturn(Optional.of(emptyDepot));
        when(floatingAverageCalculator.calculate(BITCOIN, null)).thenReturn(BITCOIN_AVERAGE);

        job.execute();

        verify(depotRepository).store(DEPOT);
        verifyNoMoreInteractions(deviationAlert);
    }

    @Test
    public void should_be_able_to_calculate_if_initial_depot_does_not_exist() throws Exception {
        when(depotRepository.load("test-depot")).thenReturn(Optional.empty());
        when(floatingAverageCalculator.calculate(BITCOIN, null)).thenReturn(BITCOIN_AVERAGE);

        job.execute();

        verify(depotRepository).store(DEPOT);
        verifyNoMoreInteractions(deviationAlert);
    }

    private final static class TestMetricWriter extends CloudWatchMetricsWriter {

        private TestMetricWriter() {
            super(mock(AmazonCloudWatch.class));
        }
    }
}