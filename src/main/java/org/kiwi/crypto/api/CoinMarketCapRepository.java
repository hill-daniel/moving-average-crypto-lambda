package org.kiwi.crypto.api;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collection;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.crypto.currency.CurrencyMapper;
import org.kiwi.rest.RestClient;

public class CoinMarketCapRepository implements CurrencyRepository {

    private final RestClient restClient;
    private final CurrencyMapper<String> currencyMapper;
    private final String endpoint;

    @Inject
    CoinMarketCapRepository(RestClient restClient, @CoinMarketCap CurrencyMapper<String> currencyMapper,
            @Named(value = "crypto.coin.market.cap.endpoint") String endpoint) {
        this.restClient = restClient;
        this.currencyMapper = currencyMapper;
        this.endpoint = endpoint;
    }

    @Override
    public Collection<Currency> retrieveCurrencies() {
        try {
            String response = restClient.getResponseAsJsonFrom(endpoint);
            return currencyMapper.map(response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load crypto currencies with " + this.getClass().getSimpleName(), e);
        }
    }
}
