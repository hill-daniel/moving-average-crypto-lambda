package org.kiwi.alert;

import static java.util.Arrays.asList;
import static org.kiwi.proto.FloatingAverageProtos.FloatingAverage.newBuilder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.config.EnvironmentVariables;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.kiwi.proto.FloatingAverageTestData;

public class SNSAlertTest {

    private static final String TOPIC_ENV_VARIABLE = "snsn.topic.env";
    private static final String TOPIC_ARN = "rn:aws:sns:eu-central-1:12345:example";

    private AmazonSNSClient snsClient;
    private SNSAlert snsAlert;

    @Before
    public void setUp() throws Exception {
        snsClient = mock(AmazonSNSClient.class);
        EnvironmentVariables environmentVariables = mock(EnvironmentVariables.class);
        when(environmentVariables.getValueFrom(TOPIC_ENV_VARIABLE)).thenReturn(Optional.of(TOPIC_ARN));
        snsAlert = new SNSAlert(snsClient, TOPIC_ENV_VARIABLE, environmentVariables);
    }

    @Test
    public void should_recommend_buy_if_asset_value_is_higher_than_average() throws Exception {
        FloatingAverage floatingAverage = FloatingAverageTestData.createBitcoinTestData();
        FloatingAverage bitcoinTestData = newBuilder(floatingAverage)
                .setDeviationThreshold("5.0")
                .setLatestQuoteValue("1234.55")
                .setLatestAverage("1134.23").build();

        snsAlert.alert(bitcoinTestData);

        String expectedMessage = "Recommendation: buy Bitcoin!\n"
                + "The current closing value 1234.55 of observed asset Bitcoin "
                + "deviates more than 5.0 percent from the current floating average of 1134.23.";
        PublishRequest expectedPublishRequest = new PublishRequest()
                .withTopicArn(TOPIC_ARN)
                .withSubject("Floating Average Info - #NOFOMO")
                .withMessage(expectedMessage);
        verify(snsClient).publish(expectedPublishRequest);
    }

    @Test
    public void should_recommend_sell_if_asset_value_is_lower_than_average() throws Exception {
        FloatingAverage bitcoinTestData = FloatingAverageTestData.createBitcoinTestData();

        snsAlert.alert(bitcoinTestData);

        String expectedMessage = "Recommendation: sell Bitcoin!\n"
                + "The current closing value 1210.02 of observed asset Bitcoin "
                + "deviates more than 4.0 percent from the current floating average of 1229.68.";
        PublishRequest expectedPublishRequest = new PublishRequest()
                .withTopicArn(TOPIC_ARN)
                .withSubject("Floating Average Info - #NOFOMO")
                .withMessage(expectedMessage);
        verify(snsClient).publish(expectedPublishRequest);
    }

    @Test
    public void should_send_multiple_averages_in_one_message() throws Exception {
        FloatingAverage floatingAverage = FloatingAverageTestData.createBitcoinTestData();
        FloatingAverage bitcoinTestData = newBuilder(floatingAverage)
                .setDeviationThreshold("5.0")
                .setLatestQuoteValue("1234.55")
                .setLatestAverage("1134.23").build();

        snsAlert.alert(asList(bitcoinTestData, bitcoinTestData));

        String expectedMessage = "Recommendation: buy Bitcoin!\n"
                + "The current closing value 1234.55 of observed asset Bitcoin "
                + "deviates more than 5.0 percent from the current floating average of 1134.23.\n\n" +
                "Recommendation: buy Bitcoin!\n"
                + "The current closing value 1234.55 of observed asset Bitcoin "
                + "deviates more than 5.0 percent from the current floating average of 1134.23.";
        PublishRequest expectedPublishRequest = new PublishRequest()
                .withTopicArn(TOPIC_ARN)
                .withSubject("Floating Average Info - #NOFOMO")
                .withMessage(expectedMessage);
        verify(snsClient).publish(expectedPublishRequest);
    }

    @Test
    public void should_not_publish_if_averages_are_emtpy() throws Exception {
        snsAlert.alert(Collections.emptyList());

        verifyNoMoreInteractions(snsClient);
    }
}