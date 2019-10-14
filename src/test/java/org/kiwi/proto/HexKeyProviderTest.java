package org.kiwi.proto;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.Before;
import org.junit.Test;

public class HexKeyProviderTest {

    private HexKeyProvider hexKeyProvider;

    @Before
    public void setUp() throws Exception {
        hexKeyProvider = new HexKeyProvider();
    }

    @Test
    public void should_create_hex_key_from_id() throws Exception {
        String key = hexKeyProvider.createKeyFor("bitcoin");

        assertThat(key).isEqualTo("626974636f696e_bitcoin");
    }

    @Test
    public void should_throw_illegal_argument_exception_if_id_is_null() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> hexKeyProvider.createKeyFor(null));
    }

    @Test
    public void should_throw_illegal_argument_exception_if_id_is_empty() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> hexKeyProvider.createKeyFor(""));
    }
}