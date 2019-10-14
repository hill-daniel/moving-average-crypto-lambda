package org.kiwi.calculation;

import static java.util.Comparator.comparingLong;
import static java.util.stream.Collectors.toList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.inject.Inject;
import org.kiwi.config.Configuration;
import org.kiwi.config.ConfigurationLoader;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState;
import org.kiwi.proto.FloatingAverageProtos.Quote;

class FloatingAverageCalculator {

    private final ConfigurationLoader configurationLoader;

    @Inject
    FloatingAverageCalculator(ConfigurationLoader configurationLoader) {
        this.configurationLoader = configurationLoader;
    }

    FloatingAverage calculate(Currency currency, FloatingAverage floatingAverage) {
        BigDecimal latestValue = currency.priceInUsDollar();
        long dateInEpochSeconds = currency.lastUpdated().getEpochSecond();
        Collection<Quote> historicalQuotes = getHistoricalQuotes(floatingAverage);
        BigDecimal latestAverage = calculateLatestAverageWith(latestValue, historicalQuotes);
        Quote latestQuote = createQuoteWith(latestValue, latestAverage, dateInEpochSeconds);
        historicalQuotes.add(latestQuote);
        Configuration configuration = configurationLoader.loadFor(currency.id());
        BigDecimal deviationThreshold = configuration.deviationThreshold();
        AlertState state = evaluateAlertState(floatingAverage, latestValue, latestAverage, deviationThreshold);

        return newBuilder()
                .setId(currency.id())
                .setSymbol(currency.symbol())
                .setName(currency.name())
                .setClosingDate(dateInEpochSeconds)
                .setDeviationThreshold(deviationThreshold.toPlainString())
                .setLatestAverage(latestAverage.toPlainString())
                .setLatestQuoteValue(latestValue.toPlainString())
                .setMaxDaysCap(configuration.maxDaysCap())
                .addAllQuotes(historicalQuotes)
                .setAlertState(state)
                .build();
    }

    private BigDecimal calculateLatestAverageWith(BigDecimal latestValue, Collection<Quote> historicalQuotes) {
        List<BigDecimal> quoteValues = historicalQuotes.stream()
                .map(quote -> new BigDecimal(quote.getValue()))
                .collect(toList());
        quoteValues.add(latestValue);
        return new Average().calculateFor(quoteValues);
    }

    private Collection<Quote> getHistoricalQuotes(FloatingAverage floatingAverage) {
        if (floatingAverage != null) {
            List<Quote> quotesList = new ArrayList<>(floatingAverage.getQuotesList());
            if (isMaxDaysCapReached(floatingAverage, quotesList)) {
                removeOldestQuoteFrom(quotesList);
            }
            return quotesList;
        }
        return new ArrayList<>();
    }

    private boolean isMaxDaysCapReached(FloatingAverage floatingAverage, List<Quote> quotesList) {
        return quotesList.size() != 0 && quotesList.size() == floatingAverage.getMaxDaysCap();
    }

    private Quote createQuoteWith(BigDecimal latestValue, BigDecimal latestAverage, long dateInEpochSeconds) {
        return Quote.newBuilder()
                .setAverage(latestAverage.toPlainString())
                .setValue(latestValue.toPlainString())
                .setUpdatedAt(dateInEpochSeconds)
                .build();
    }

    private void removeOldestQuoteFrom(List<Quote> quotesList) {
        quotesList.sort(comparingLong(Quote::getUpdatedAt));
        quotesList.remove(0);
    }

    private AlertState evaluateAlertState(FloatingAverage floatingAverage, BigDecimal latestValue,
            BigDecimal latestAverage, BigDecimal deviationThreshold) {
        AlertState state = new AverageAlertState()
                .evaluateStateWith(latestAverage, latestValue, deviationThreshold);
        if (floatingAverage != null && state == NONE) {
            state = floatingAverage.getAlertState();
        }
        return state;
    }
}
