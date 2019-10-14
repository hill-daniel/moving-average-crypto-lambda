package org.kiwi.calculation;


import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.config.Configuration.newConfiguration;
import static org.kiwi.crypto.currency.Currency.newCurrency;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.SELL;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.kiwi.proto.FloatingAverageTestData.createEthereumTestData;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.config.ConfigurationLoader;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;

public class FloatingAverageCalculatorTest {

    private FloatingAverageCalculator calculator;

    @Before
    public void setUp() throws Exception {
        ConfigurationLoader configurationLoader = mock(ConfigurationLoader.class);
        when(configurationLoader.loadFor(anyString())).thenReturn(newConfiguration(new BigDecimal("4.0"), 200));
        calculator = new FloatingAverageCalculator(configurationLoader);
    }

    @Test
    public void should_create_average_if_no_historical_quotes_are_given() throws Exception {
        Currency etherum = newCurrency("ethereum", "Ethereum", "ETH", new BigDecimal("18.2812"),
                LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC), 0);
        FloatingAverage averagWithoutQuotes = newBuilder(createEthereumTestData())
                .clearQuotes()
                .build();

        FloatingAverage latestAverage = calculator.calculate(etherum, averagWithoutQuotes);

        Quote etherumQuote = Quote.newBuilder()
                .setAverage("18.2812")
                .setValue("18.2812")
                .setUpdatedAt(1485129600)
                .build();
        FloatingAverage expectedAverage = FloatingAverage.newBuilder()
                .setId("ethereum")
                .setName("Ethereum")
                .setSymbol("ETH")
                .setAlertState(NONE)
                .setClosingDate(1485129600)
                .setMaxDaysCap(200)
                .setDeviationThreshold("4.0")
                .setLatestAverage("18.2812")
                .setLatestQuoteValue("18.2812")
                .addQuotes(etherumQuote)
                .build();
        assertThat(latestAverage).isEqualTo(expectedAverage);
    }

    @Test
    public void should_append_latest_to_historical_quotes_and_calculate_average() throws Exception {
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
                LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC), 0);
        FloatingAverage bitcoinAverage = createBitcoinTestData();

        FloatingAverage latestAverage = calculator.calculate(bitcoin, bitcoinAverage);

        Quote expectedNewQuote = Quote.newBuilder()
                .setValue("1229.68")
                .setAverage("1213.82")
                .setUpdatedAt(1485129600).build();
        FloatingAverage expectedAverage = FloatingAverage.newBuilder(bitcoinAverage)
                .setDeviationThreshold("4.0")
                .setLatestAverage("1213.82")
                .setLatestQuoteValue("1229.68")
                .setClosingDate(1485129600)
                .addQuotes(expectedNewQuote)
                .setMaxDaysCap(200)
                .build();
        assertThat(latestAverage).isEqualTo(expectedAverage);
    }

    @Test
    public void should_replace_oldest_quote_with_recent_one_if_max_cap_is_reached() throws Exception {
        FloatingAverage floatingAverageWithCap = FloatingAverage.newBuilder(createBitcoinTestData())
                .setMaxDaysCap(2)
                .build();
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
                LocalDate.of(2017, 1, 24).atStartOfDay().toInstant(UTC), 0);

        FloatingAverage latestAverage = calculator.calculate(bitcoin, floatingAverageWithCap);

        assertThat(latestAverage.getQuotesCount()).isEqualTo(2);
        Quote replacedQuote = Quote.newBuilder()
                .setAverage("1199.67")
                .setValue("1201.75")
                .setUpdatedAt(1485043200L)
                .build();
        assertThat(latestAverage.getQuotesList()).doesNotContain(replacedQuote);
    }

    @Test
    public void should_keep_old_state_if_new_evaluates_to_none() throws Exception {
        FloatingAverage floatingAverageSell = FloatingAverage.newBuilder(createBitcoinTestData())
                .setAlertState(SELL).build();
        Currency bitcoin = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1229.68"),
                LocalDate.of(2017, 1, 24).atStartOfDay().toInstant(UTC), 0);

        FloatingAverage latestAverage = calculator.calculate(bitcoin, floatingAverageSell);

        assertThat(latestAverage.getAlertState()).isEqualTo(SELL);

    }
}