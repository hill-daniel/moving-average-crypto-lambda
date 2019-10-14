package org.kiwi.alert;

import static java.util.stream.Collectors.joining;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.math.BigDecimal;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.kiwi.config.EnvironmentVariables;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;

public class SNSAlert implements DeviationAlert {

    private static final Logger LOGGER = Logger.getLogger(SNSAlert.class);

    private final AmazonSNS snsClient;
    private final String topic;

    @Inject
    SNSAlert(AmazonSNS snsClient, @Named("sns.alert.topic.arn.env.variable") String topicEnvVariable,
            EnvironmentVariables environmentVariables) {
        this.snsClient = snsClient;
        this.topic = environmentVariables.getValueFrom(topicEnvVariable)
                .orElseThrow(() -> new RuntimeException(
                        "Failed to load SNS topic from environment with key [" + topicEnvVariable + "]"));
    }

    @Override
    public void alert(FloatingAverage floatingAverage) {
        if (floatingAverage == null) {
            return;
        }
        String messageBody = createMessageBodyWith(floatingAverage);
        publish(messageBody);
    }

    @Override
    public void alert(Collection<FloatingAverage> floatingAverages) {
        if (floatingAverages.isEmpty()) {
            return;
        }
        String multipleMessagesBody = floatingAverages.stream()
                .map(this::createMessageBodyWith)
                .collect(joining("\n\n"));
        publish(multipleMessagesBody);
    }

    private PublishRequest createPublishRequestWith(String messageBody) {
        return new PublishRequest()
                .withTopicArn(topic)
                .withSubject("Floating Average Info - #NOFOMO")
                .withMessage(messageBody);
    }

    private String createMessageBodyWith(FloatingAverage floatingAverage) {
        String recommendation = isValueHigherThanAverage(floatingAverage) ? "buy" : "sell";
        return "Recommendation: " + recommendation + " " + floatingAverage.getName() + "!\n" +
                "The current closing value " + floatingAverage.getLatestQuoteValue() +
                " of observed asset " + floatingAverage.getName() +
                " deviates more than " + floatingAverage.getDeviationThreshold() +
                " percent from the current floating average of " + floatingAverage.getLatestAverage() + ".";
    }

    private void publish(String messageBody) {
        PublishRequest publishRequest = createPublishRequestWith(messageBody);
        try {
            snsClient.publish(publishRequest);
        } catch (Exception e) {
            LOGGER.error("Failed to publish message [" + messageBody + "] to topic [" + topic + "]", e);
        }
    }

    private boolean isValueHigherThanAverage(FloatingAverage floatingAverage) {
        BigDecimal latestQuoteValue = new BigDecimal(floatingAverage.getLatestQuoteValue());
        BigDecimal latestAverage = new BigDecimal(floatingAverage.getLatestAverage());
        return latestQuoteValue.compareTo(latestAverage) == 1;
    }
}
