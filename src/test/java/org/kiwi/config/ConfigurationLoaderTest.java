package org.kiwi.config;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.kiwi.config.Configuration.newConfiguration;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationLoaderTest {

    private Map<String, Configuration> idToConfiguration;
    private ConfigurationLoader loader;

    @Before
    public void setUp() throws Exception {
        idToConfiguration = new HashMap<>();
        loader = new ConfigurationLoader(idToConfiguration);
    }

    @Test
    public void should_load_config_for_key() throws Exception {
        Configuration expectedConfiguration = newConfiguration(new BigDecimal("5.3"), 150);
        idToConfiguration.put("ethereum", expectedConfiguration);

        Configuration ethereumConfig = loader.loadFor("ethereum");

        assertThat(ethereumConfig).isEqualTo(expectedConfiguration);
    }

    @Test
    public void should_load_default_key_if_no_configuration_for_key_is_found() throws Exception {
        Configuration expectedConfiguration = newConfiguration(new BigDecimal("7.3"), 200);
        idToConfiguration.put("default", expectedConfiguration);

        Configuration ethereumConfig = loader.loadFor("ethereum");

        assertThat(ethereumConfig).isEqualTo(expectedConfiguration);
    }

    @Test
    public void should_fail_if_neither_value_nor_default_can_be_loaded() throws Exception {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> loader.loadFor("bitcoin"))
                .withMessage("Failed to load Default configuration for [bitcoin]");
    }
}
