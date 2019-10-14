package org.kiwi.retry;

import java.util.concurrent.Callable;

@FunctionalInterface
public interface RetryStrategy<T> {

    T execute(Callable<T> callable);
}
