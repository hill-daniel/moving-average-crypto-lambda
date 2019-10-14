package org.kiwi.retry;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Test;

public class ExponentialBackoffStrategyTest {

    @Test
    public void should_return_value_on_successful_attempt() throws Exception {
        ExponentialBackoffStrategy<String> backoffStrategy = new ExponentialBackoffStrategy<>(
                singleton(IOException.class), new int[]{1});

        String value = backoffStrategy.execute(() -> "value");

        assertThat(value).isEqualTo("value");
    }

    @Test
    public void should_retry_on_defined_exception() throws Exception {
        ExponentialBackoffStrategy<String> backoffStrategy = new ExponentialBackoffStrategy<>(
                singleton(IOException.class), new int[]{1});
        ExceptionThrowingCallable callable = newCallable().shouldFailTimes(1);

        String value = backoffStrategy.execute(callable);

        assertThat(value).isEqualTo("success");
        assertThat(callable.getExecutions()).isEqualTo(2);
    }

    @Test
    public void should_not_retry_more_times_than_array_length() throws Exception {
        ExponentialBackoffStrategy<String> backoffStrategy = new ExponentialBackoffStrategy<>(
                singleton(IOException.class), new int[]{1, 1});
        ExceptionThrowingCallable callable = newCallable().shouldFailTimes(3);

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> backoffStrategy.execute(callable));
        assertThat(callable.getExecutions()).isEqualTo(3);
    }

    @Test
    public void should_fail_on_unknown_exception() throws Exception {
        ExponentialBackoffStrategy<String> backoffStrategy = new ExponentialBackoffStrategy<>(
                singleton(IOException.class), new int[]{1, 2, 3});

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(
                () -> backoffStrategy.execute(() -> {
                    throw new RuntimeException("Failed");
                }));
    }

    private ExceptionThrowingCallable newCallable() {
        return new ExceptionThrowingCallable();
    }

    private static class ExceptionThrowingCallable implements Callable<String> {

        private int failTimes = 0;
        private int executions = 0;

        private ExceptionThrowingCallable() {
        }

        ExceptionThrowingCallable shouldFailTimes(int times) {
            failTimes = times;
            return this;
        }

        int getExecutions() {
            return executions;
        }

        @Override
        public String call() throws Exception {
            if (executions++ < failTimes) {
                IOException ioException = new IOException("Failed to read i/o");
                throw new RuntimeException("Failed", ioException);
            }
            return "success";
        }
    }
}