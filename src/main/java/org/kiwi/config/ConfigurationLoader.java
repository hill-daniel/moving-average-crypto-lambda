package org.kiwi.config;

import java.util.Map;

public class ConfigurationLoader {

    private static final String DEFAULT = "default";
    private final Map<String, Configuration> currencyIdToConfiguration;

    public ConfigurationLoader(
            Map<String, Configuration> currencyIdToConfiguration) {
        this.currencyIdToConfiguration = currencyIdToConfiguration;
    }

    public Configuration loadFor(String currencyId) {
        Configuration configuration = currencyIdToConfiguration.get(currencyId);
        if (configuration == null) {
            configuration = currencyIdToConfiguration.get(DEFAULT);
        }
        if (configuration == null) {
            throw new RuntimeException("Failed to load Default configuration for [" + currencyId + "]");
        }
        return configuration;
    }
}
