package org.kiwi.crypto.currency;

import static java.math.BigDecimal.ZERO;
import static java.time.Instant.ofEpochSecond;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.kiwi.crypto.currency.Currency.newCurrency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.function.Function;

public class CoinMarketCapMapper implements CurrencyMapper<String> {

    private static final String NO_VALUE = "NO_VALUE";

    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Inject
    CoinMarketCapMapper(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public Collection<Currency> map(String value) {
        try {
            JsonNode jsonNode = objectMapper.readTree(value);
            return stream(spliteratorUnknownSize(jsonNode.iterator(), ORDERED), false)
                    .map(toCurrency())
                    .collect(toSet());
        } catch (IOException e) {
            throw new RuntimeException("Failed to map " + value + " to Currency", e);
        }
    }

    private Function<JsonNode, Currency> toCurrency() {
        return node -> {
            try {
                String id = parseTextFrom(node, "id");
                String name = parseTextFrom(node, "name");
                String symbol = parseTextFrom(node, "symbol");
                BigDecimal priceInUsDollar = parsePriceFrom(node);
                Instant lastUpdated = parseInstantFrom(node);
                int rank = parseRankFrom(node);
                return newCurrency(id, name, symbol, priceInUsDollar, lastUpdated, rank);
            } catch (Exception e) {
                throw new RuntimeException("could not parse " + node);
            }
        };
    }

    private String parseTextFrom(JsonNode node, String fieldName) {
        JsonNode jsonNode = node.get(fieldName);
        return jsonNode != null && !jsonNode.isNull() ? jsonNode.asText() : NO_VALUE;
    }

    private Instant parseInstantFrom(JsonNode node) {
        JsonNode jsonNode = node.get("last_updated");
        return jsonNode != null && !jsonNode.isNull() ? ofEpochSecond(jsonNode.asLong()) : clock.instant();
    }

    private BigDecimal parsePriceFrom(JsonNode node) {
        JsonNode jsonNode = node.get("price_usd");
        return jsonNode != null && !jsonNode.isNull() ? new BigDecimal(jsonNode.asText()) : ZERO;
    }

    private int parseRankFrom(JsonNode node) {
        JsonNode jsonNode = node.get("rank");
        return jsonNode != null && !jsonNode.isNull() ? jsonNode.asInt() : 0;
    }

}
