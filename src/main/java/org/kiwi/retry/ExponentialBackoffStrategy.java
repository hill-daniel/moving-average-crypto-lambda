package org.kiwi.retry;

import static java.lang.Thread.sleep;

import java.util.Collection;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

public class ExponentialBackoffStrategy<T> implements RetryStrategy<T> {

    private static final Logger LOGGER = Logger.getLogger(ExponentialBackoffStrategy.class);
    private static final int SECONDS_IN_MILLISECONDS = 1000;

    private final Collection<Class<? extends Throwable>> backoffExceptions;
    private final int[] delayBetweenAttempts;

    public ExponentialBackoffStrategy(Collection<Class<? extends Throwable>> backoffExceptionCause,
            int[] delayBetweenAttempts) {
        this.backoffExceptions = backoffExceptionCause;
        this.delayBetweenAttempts = delayBetweenAttempts;
    }

    public T execute(Callable<T> callable) {
        return executeWithBackoff(0, callable);
    }

    private T executeWithBackoff(int attempt, Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception potentialBackoffException) {
            Throwable exceptionCause = potentialBackoffException.getCause();
            if (exceptionCause != null && shouldTryAgain(attempt) && isBackoffException(exceptionCause)) {
                LOGGER.warn("Execution of [" + callable.getClass() + "] failed. Will retry", exceptionCause);
                return retry(attempt, callable);
            } else {
                throw new RuntimeException(potentialBackoffException);
            }
        }
    }

    private boolean shouldTryAgain(int attempt) {
        return attempt < delayBetweenAttempts.length;
    }

    private boolean isBackoffException(Throwable exceptionCause) {
        return backoffExceptions.contains(exceptionCause.getClass());
    }

    private T retry(int attempt, Callable<T> callable) {
        try {
            sleep(delayBetweenAttempts[attempt] * SECONDS_IN_MILLISECONDS);
            return executeWithBackoff(attempt + 1, callable);
        } catch (InterruptedException e) {
            throw new RuntimeException("Failed, got interrupted", e);
        }
    }
}
