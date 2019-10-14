package org.kiwi;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static com.google.inject.name.Names.bindProperties;
import static java.util.Collections.singleton;

import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchAsyncClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSAsyncClientBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.time.Clock;
import java.util.HashMap;
import java.util.Properties;
import org.kiwi.alert.DeviationAlert;
import org.kiwi.alert.SNSAlert;
import org.kiwi.aws.s3.BinaryBucket;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3KeyProvider;
import org.kiwi.config.Configuration;
import org.kiwi.config.ConfigurationLoader;
import org.kiwi.config.EnvironmentVariables;
import org.kiwi.crypto.api.CoinMarketCap;
import org.kiwi.crypto.api.CoinMarketCapRepository;
import org.kiwi.crypto.api.CurrencyRepository;
import org.kiwi.crypto.currency.CoinMarketCapMapper;
import org.kiwi.crypto.currency.CurrencyMapper;
import org.kiwi.proto.DepotRepository;
import org.kiwi.proto.DepotS3Repository;
import org.kiwi.proto.HexKeyProvider;
import org.kiwi.rest.RestClient;
import org.kiwi.rest.RetryingRestClient;
import org.kiwi.retry.ExponentialBackoffStrategy;
import org.kiwi.retry.RetryStrategy;

public class FloatingAverageLambdaModule extends AbstractModule {

    private static final String CONFIG_PROPERTIES = "/config.properties";
    private static final String CURRENCY_CONFIG = "currency.json";

    @Override
    protected void configure() {
        bindProperties(binder(), loadProperties(CONFIG_PROPERTIES));
        bind(CurrencyRepository.class)
                .annotatedWith(CoinMarketCap.class)
                .to(CoinMarketCapRepository.class);
        bind(new TypeLiteral<CurrencyMapper<String>>() {
        })
                .annotatedWith(CoinMarketCap.class)
                .to(CoinMarketCapMapper.class);
        bind(RestClient.class)
                .to(RetryingRestClient.class);
        bind(S3Bucket.class)
                .to(BinaryBucket.class);
        bind(S3KeyProvider.class)
                .to(HexKeyProvider.class);
        bind(DepotRepository.class)
                .to(DepotS3Repository.class);
        bind(DeviationAlert.class)
                .to(SNSAlert.class);
    }

    @Provides
    @Singleton
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new Jdk8Module())
                .registerModule(new JavaTimeModule())
                .disable(WRITE_DATES_AS_TIMESTAMPS);
    }

    @Provides
    @Singleton
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Provides
    @Singleton
    public AmazonSNS amazonSNSClient() {
        return AmazonSNSAsyncClientBuilder.defaultClient();
    }

    @Provides
    @Singleton
    public AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.defaultClient();
    }

    @Provides
    @Singleton
    public AmazonCloudWatch cloudWatchClient() {
        return AmazonCloudWatchAsyncClientBuilder.defaultClient();
    }

    @Provides
    @Singleton
    public ConfigurationLoader configurationLoader(ObjectMapper objectMapper,
            @Named("aws.s3.bucket.name") String bucketName, AmazonS3 s3client) {
        TypeReference<HashMap<String, Configuration>> stringToConfigTypeRef
                = new TypeReference<HashMap<String, Configuration>>() {
        };
        GetObjectRequest currencyConfigRequest = new GetObjectRequest(bucketName, CURRENCY_CONFIG);
        S3Object object = s3client.getObject(currencyConfigRequest);
        try (InputStream currencyConfigStream = object.getObjectContent()) {
            HashMap<String, Configuration> currencyIdToConfig = objectMapper
                    .readValue(currencyConfigStream, stringToConfigTypeRef);
            return new ConfigurationLoader(currencyIdToConfig);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load currency configuration from file [" + CURRENCY_CONFIG + "]");
        }
    }

    @Provides
    @Singleton
    EnvironmentVariables environmentVariables() {
        return new EnvironmentVariables(System.getenv());
    }

    @Provides
    @Singleton
    RetryStrategy<String> retryStrategy() {
        return new ExponentialBackoffStrategy<>(singleton(SocketTimeoutException.class), new int[]{10, 30, 60});
    }

    private Properties loadProperties(String propertiesFile) {
        try {
            Properties properties = new Properties();
            try (InputStream inputStream = getClass().getResourceAsStream(propertiesFile)) {
                properties.load(inputStream);
                return properties;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load properties from file [" + propertiesFile + "]", e);
        }
    }
}
