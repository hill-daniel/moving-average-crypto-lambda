package org.kiwi.crypto.api;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.crypto.currency.Currency;
import org.kiwi.crypto.currency.CurrencyMapper;
import org.kiwi.rest.RestClient;

public class CoinMarketCapRepositoryTest {

    private static final String ENDPOINT = "someEndpoint";

    private CoinMarketCapRepository coinMarketCapRepository;
    private CurrencyMapper<String> mapper;
    private RestClient restClient;

    @Before
    public void setUp() throws Exception {
        restClient = mock(RestClient.class);
        //noinspection unchecked
        mapper = (CurrencyMapper<String>) mock(CurrencyMapper.class);
        coinMarketCapRepository = new CoinMarketCapRepository(restClient, mapper, ENDPOINT);
    }

    @Test
    public void should_call_rest_client_and_mapper_and_return_result() throws Exception {
        when(restClient.getResponseAsJsonFrom(ENDPOINT)).thenReturn("restAnswer");
        when(mapper.map("restAnswer")).thenReturn(emptySet());

        Collection<Currency> currencies = coinMarketCapRepository.retrieveCurrencies();

        verify(restClient).getResponseAsJsonFrom(ENDPOINT);
        verifyNoMoreInteractions(restClient);
        verify(mapper).map("restAnswer");
        verifyNoMoreInteractions(mapper);
        assertThat(currencies).isEmpty();
    }
}