package org.kiwi.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.value.AutoValue;
import java.math.BigDecimal;

@AutoValue
public abstract class Configuration {

    @JsonCreator
    public static Configuration newConfiguration(
            @JsonProperty(value = "deviationThreshold") BigDecimal deviationThreshold,
            @JsonProperty(value = "maxDaysCap") int maxDaysCap) {
        return new AutoValue_Configuration(deviationThreshold, maxDaysCap);
    }

    public abstract BigDecimal deviationThreshold();

    public abstract int maxDaysCap();
}
