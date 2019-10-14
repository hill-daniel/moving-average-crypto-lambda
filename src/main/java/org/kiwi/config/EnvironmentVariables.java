package org.kiwi.config;

import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;

public class EnvironmentVariables {

    private final Map<String, String> envKeyToValue;

    @Inject
    public EnvironmentVariables(Map<String, String> envKeyToValue) {
        this.envKeyToValue = envKeyToValue;
    }

    public Optional<String> getValueFrom(String key) {
        return Optional.of(envKeyToValue.get(key));
    }
}
