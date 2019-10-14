package org.kiwi.rest;

import com.google.inject.Inject;
import org.kiwi.retry.RetryStrategy;

public class RetryingRestClient extends UnirestClient {

    private final RetryStrategy<String> retryStrategy;

    @Inject
    RetryingRestClient(RetryStrategy<String> retryStrategy) {
        this.retryStrategy = retryStrategy;
    }

    @Override
    public String getResponseAsJsonFrom(String endpoint) {
        return retryStrategy.execute(() -> super.getResponseAsJsonFrom(endpoint));
    }
}
