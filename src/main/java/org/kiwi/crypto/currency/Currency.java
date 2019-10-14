package org.kiwi.crypto.currency;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.math.BigDecimal;
import java.time.Instant;

@AutoValue
public abstract class Currency {

    @JsonCreator
    public static Currency newCurrency(@JsonProperty String id, @JsonProperty String name, @JsonProperty String symbol,
            @JsonProperty BigDecimal priceInUsDollar, @JsonProperty Instant lastUpdated, @JsonProperty int rank) {
        return new AutoValue_Currency(id, name, symbol, priceInUsDollar, lastUpdated, rank);
    }

    public abstract String id();

    public abstract String name();

    public abstract String symbol();

    public abstract BigDecimal priceInUsDollar();

    public abstract Instant lastUpdated();

    public abstract int rank();
}
