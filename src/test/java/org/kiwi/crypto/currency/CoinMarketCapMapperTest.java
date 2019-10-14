package org.kiwi.crypto.currency;

import static java.time.Clock.fixed;
import static java.time.Instant.now;
import static java.time.Instant.ofEpochSecond;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.crypto.currency.Currency.newCurrency;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.ZoneId;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.FloatingAverageLambdaModule;

public class CoinMarketCapMapperTest {

    private CoinMarketCapMapper mapper;
    private Clock clock;

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new FloatingAverageLambdaModule().objectMapper();
        clock = fixed(now(), ZoneId.systemDefault());
        mapper = new CoinMarketCapMapper(objectMapper, clock);
    }

    @Test
    public void should_map_json_to_currency() throws Exception {
        String json = "[{\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"price_usd\": \"1253.84\", \n" +
                "        \"rank\": \"1\", \n" +
                "        \"last_updated\": \"1488719045\"\n" +
                "    }]";
        Currency expected = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1253.84"),
                ofEpochSecond(1488719045), 1);

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_replace_missing_text_values_with_NO_VALUE() throws Exception {
        String json = "[{\n" +
                "        \"price_usd\": \"1253.84\", \n" +
                "        \"last_updated\": \"1488719045\"\n" +
                "    }]";
        Currency expected = newCurrency("NO_VALUE", "NO_VALUE", "NO_VALUE", new BigDecimal("1253.84"),
                ofEpochSecond(1488719045), 0);

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_replace_missing_updated_value_with_current_time() throws Exception {
        String json = "[{\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"price_usd\": \"1253.84\"\n" +
                "    }]";
        Currency expected = newCurrency("bitcoin", "Bitcoin", "BTC", new BigDecimal("1253.84"), clock.instant(), 0);

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_replace_missing_currency_value_with_zero() throws Exception {
        String json = "[{\n" +
                "        \"id\": \"bitcoin\", \n" +
                "        \"name\": \"Bitcoin\", \n" +
                "        \"symbol\": \"BTC\", \n" +
                "        \"last_updated\": \"1488719045\"\n" +
                "    }]";
        Currency expected = newCurrency("bitcoin", "Bitcoin", "BTC", BigDecimal.ZERO, ofEpochSecond(1488719045), 0);

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isEqualTo(singleton(expected));
    }

    @Test
    public void should_parse_goddamn_vcoin() throws Exception {
        String json = "{\"id\":\"vcoin\",\"name\":\"Vcoin\",\"symbol\":\"VCN\",\"rank\":\"737\",\"price_usd\":null,\"price_btc\":null,\"24h_volume_usd\":null,\"market_cap_usd\":null,\"available_supply\":null,\"total_supply\":null,\"percent_change_1h\":null,\"percent_change_24h\":null,\"percent_change_7d\":null,\"last_updated\":null}";

        Collection<Currency> currency = mapper.map(json);

        assertThat(currency).isNotNull();
    }
}