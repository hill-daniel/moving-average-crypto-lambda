package org.kiwi.aws.metrics;

import static com.amazonaws.services.cloudwatch.model.StandardUnit.Milliseconds;
import static java.lang.System.currentTimeMillis;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import com.google.inject.Inject;
import java.util.function.Supplier;
import org.apache.log4j.Logger;

public class CloudWatchMetricsWriter {

    private static final Logger LOGGER = Logger.getLogger(CloudWatchMetricsWriter.class);
    private static final String NAMESPACE = "floating-average-crypto-metrics";

    private final AmazonCloudWatch cloudWatchClient;

    @Inject
    public CloudWatchMetricsWriter(AmazonCloudWatch cloudWatchClient) {
        this.cloudWatchClient = cloudWatchClient;
    }

    public <T> T executeWithMetric(Supplier<T> action, String metricName) {
        long start = currentTimeMillis();
        T value = action.get();
        long end = currentTimeMillis();
        writeExecutionTimeFor(metricName, end - start);
        return value;
    }

    public void executeWithMetric(Runnable action, String metricName) {
        long start = currentTimeMillis();
        action.run();
        long end = currentTimeMillis();
        writeExecutionTimeFor(metricName, end - start);
    }

    public void writeExecutionTimeFor(String metricName, Long durationInMilliseconds) {
        try {
            MetricDatum metricDatum = createMetricDatum(metricName, durationInMilliseconds);
            write(metricDatum);
        } catch (Exception e) {
            LOGGER.error("Failed to write metric [" + metricName + "] "
                    + "with value [" + durationInMilliseconds + "] in ms to CloudWatch", e);
        }
    }

    private MetricDatum createMetricDatum(String metricName, Long durationInMilliseconds) {
        Dimension executionDurationDimension = new Dimension()
                .withName("EXECUTION_DURATION")
                .withValue("Milliseconds");
        return new MetricDatum()
                .withMetricName(metricName)
                .withDimensions(executionDurationDimension)
                .withUnit(Milliseconds)
                .withValue(durationInMilliseconds.doubleValue());
    }

    private void write(MetricDatum metric) {
        PutMetricDataRequest putMetricDataRequest = new PutMetricDataRequest()
                .withNamespace(NAMESPACE)
                .withMetricData(metric);
        cloudWatchClient.putMetricData(putMetricDataRequest);
    }
}
