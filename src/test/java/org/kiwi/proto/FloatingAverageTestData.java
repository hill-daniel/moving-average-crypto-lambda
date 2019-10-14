package org.kiwi.proto;

import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.AlertState.NONE;

import java.time.LocalDate;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageProtos.Quote;

public class FloatingAverageTestData {

    public static FloatingAverage createBitcoinTestData() {
        Quote quote1 = Quote.newBuilder()
                .setAverage("1199.67")
                .setValue("1201.75")
                .setUpdatedAt(LocalDate.of(2017, 1, 22).atStartOfDay().toInstant(UTC)
                        .getEpochSecond())
                .build();
        Quote quote2 = Quote.newBuilder()
                .setAverage("1189.92")
                .setValue("1210.02")
                .setUpdatedAt(LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC)
                        .getEpochSecond())
                .build();
        return FloatingAverage.newBuilder()
                .setId("bitcoin")
                .setName("Bitcoin")
                .setSymbol("BTC")
                .setAlertState(NONE)
                .setClosingDate(LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC)
                        .getEpochSecond())
                .setDeviationThreshold("4.0")
                .setLatestAverage("1229.68")
                .setLatestQuoteValue("1210.02")
                .addAllQuotes(asList(quote1, quote2))
                .build();
    }

    public static FloatingAverage createEthereumTestData() {
        Quote etherumQuote = Quote.newBuilder()
                .setAverage("18.2812")
                .setValue("18.2812")
                .setUpdatedAt(LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC)
                        .getEpochSecond())
                .build();
        return FloatingAverage.newBuilder()
                .setId("ethereum")
                .setName("Ethereum")
                .setSymbol("ETH")
                .setAlertState(NONE)
                .setClosingDate(LocalDate.of(2017, 1, 23).atStartOfDay().toInstant(UTC)
                        .getEpochSecond())
                .setDeviationThreshold("4.0")
                .setLatestAverage("18.2812")
                .setLatestQuoteValue("18.2812")
                .addQuotes(etherumQuote)
                .build();
    }
}
