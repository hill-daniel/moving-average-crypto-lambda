package org.kiwi.proto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.aws.s3.S3KeyProvider;
import org.kiwi.proto.FloatingAverageProtos.Depot;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.mockito.InOrder;

public class DepotS3RepositoryTest {

    private DepotS3Repository depotS3Repository;
    private S3Bucket s3Bucket;
    private S3KeyProvider keyProvider;
    private Depot depot;

    @Before
    public void setUp() throws Exception {
        s3Bucket = mock(S3Bucket.class);
        when(s3Bucket.exists(anyString())).thenReturn(true);
        keyProvider = mock(S3KeyProvider.class);
        depotS3Repository = new DepotS3Repository(s3Bucket, keyProvider);
        FloatingAverage bitcoinAverage = createBitcoinTestData();
        depot = Depot.newBuilder()
                .setId("id")
                .addFloatingAverages(bitcoinAverage)
                .build();
    }

    @Test
    public void should_load_with_key_from_key_provider() throws Exception {
        String key = "valid_key";
        when(keyProvider.createKeyFor("id")).thenReturn(key);
        S3Content content = newS3Content(key, depot.toByteArray(), "binary/octet-stream");
        when(s3Bucket.retrieveContentFor(key)).thenReturn(content);

        Optional<Depot> loadedFloatingAverage = depotS3Repository.load("id");

        InOrder order = inOrder(s3Bucket);
        order.verify(s3Bucket).exists(key);
        order.verify(s3Bucket).retrieveContentFor(key);
        assertThat(loadedFloatingAverage).isPresent().contains(depot);
    }

    @Test
    public void should_store_binary_object_with_key_from_provider() throws Exception {
        String key = "this_is_a_valid_key";
        when(keyProvider.createKeyFor(depot.getId())).thenReturn(key);
        S3Content expectedContent = newS3Content(key, depot.toByteArray(), "binary/octet-stream");

        depotS3Repository.store(depot);

        InOrder order = inOrder(s3Bucket);
        order.verify(s3Bucket).storeContent(expectedContent);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void should_not_try_to_load_if_object_does_not_exist_for_key() throws Exception {
        String key = "this_is_a_valid_key";
        when(keyProvider.createKeyFor("id")).thenReturn(key);
        when(s3Bucket.exists(key)).thenReturn(false);

        Optional<Depot> loadedFloatingAverage = depotS3Repository.load("id");

        assertThat(loadedFloatingAverage).isNotPresent();
        InOrder order = inOrder(s3Bucket);
        order.verify(s3Bucket).exists(key);
        order.verifyNoMoreInteractions();
    }
}