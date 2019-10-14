package org.kiwi.aws;

import static com.google.inject.Guice.createInjector;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.inject.Injector;
import org.kiwi.FloatingAverageLambdaModule;
import org.kiwi.calculation.FloatingAverageJob;

@SuppressWarnings("unused")
public class CloudWatchEventHandler implements RequestHandler<Object, Void> {

    private static final Injector INJECTOR = createInjector(new FloatingAverageLambdaModule());

    @Override
    public Void handleRequest(Object input, Context context) {
        FloatingAverageJob floatingAverageJob = INJECTOR.getInstance(FloatingAverageJob.class);
        floatingAverageJob.execute();
        return null;
    }
}
