package org.kiwi.aws.metrics;


import static com.amazonaws.services.cloudwatch.model.StandardUnit.Milliseconds;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.MetricDatum;
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest;
import org.junit.Before;
import org.junit.Test;

public class CloudWatchMetricsWriterTest {

    private AmazonCloudWatch cloudWatch;
    private CloudWatchMetricsWriter cloudWatchMetricsWriter;

    @Before
    public void setUp() throws Exception {
        cloudWatch = mock(AmazonCloudWatch.class);
        cloudWatchMetricsWriter = new CloudWatchMetricsWriter(cloudWatch);
    }

    @Test
    public void should_put_simple_metric_data_to_cloudwatch() throws Exception {
        cloudWatchMetricsWriter.writeExecutionTimeFor("someJobame", 5675765L);

        Dimension executionDurationDimension = new Dimension()
                .withName("EXECUTION_DURATION")
                .withValue("Milliseconds");
        MetricDatum expectedMetricDatum = new MetricDatum()
                .withMetricName("someJobame")
                .withDimensions(executionDurationDimension)
                .withUnit(Milliseconds)
                .withValue(Long.valueOf(5675765L).doubleValue());

        PutMetricDataRequest expectedPutMetricDateRequest = new PutMetricDataRequest()
                .withNamespace("floating-average-crypto-metrics")
                .withMetricData(expectedMetricDatum);
        verify(cloudWatch).putMetricData(expectedPutMetricDateRequest);
    }

    @Test
    public void should_not_propagate_exception_during_execution() throws Exception {
        doThrow(new RuntimeException("Failed"))
                .when(cloudWatch).putMetricData(any(PutMetricDataRequest.class));

        cloudWatchMetricsWriter.writeExecutionTimeFor("someJobe", 234567L);

        verify(cloudWatch).putMetricData(any(PutMetricDataRequest.class));
    }
}