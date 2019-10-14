package org.kiwi.aws.s3;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.kiwi.aws.s3.S3Content.newS3Content;
import static org.kiwi.proto.FloatingAverageTestData.createBitcoinTestData;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.junit.Before;
import org.junit.Test;
import org.kiwi.proto.FloatingAverageProtos.FloatingAverage;
import org.mockito.ArgumentCaptor;

public class BinaryBucketTest {

    private static final String BUCKET_NAME = "testBucket";
    private static String KEY = "e85ca376-8d17-493f-826b-1a5a20c88e76";

    private BinaryBucket binaryBucket;
    private AmazonS3Client s3Client;
    private FloatingAverage floatingAverage;

    @Before
    public void setUp() throws Exception {
        s3Client = mock(AmazonS3Client.class);
        binaryBucket = new BinaryBucket(s3Client, BUCKET_NAME);
        floatingAverage = createBitcoinTestData();
    }

    @Test
    public void should_store_content_with_meta_data() throws Exception {
        S3Content content = newS3Content(KEY, floatingAverage.toByteArray(), "binary/octet-stream");
        ArgumentCaptor<PutObjectRequest> putObjectRequestArgumentCaptor = ArgumentCaptor
                .forClass(PutObjectRequest.class);

        binaryBucket.storeContent(content);

        verify(s3Client).putObject(putObjectRequestArgumentCaptor.capture());
        PutObjectRequest putObjectRequest = putObjectRequestArgumentCaptor.getValue();
        assertThat(putObjectRequest.getBucketName()).isEqualTo(BUCKET_NAME);
        ObjectMetadata metadata = putObjectRequest.getMetadata();
        assertThat(metadata.getContentType()).isEqualTo("binary/octet-stream");
        assertThat(metadata.getContentMD5()).isNotEmpty();
        assertThat(metadata.getContentLength()).isEqualTo(floatingAverage.toByteArray().length);
    }

    @Test
    public void should_propagate_exception_during_storage() throws Exception {
        S3Content content = newS3Content(KEY, new byte[]{1}, "binary/octet-stream");
        doThrow(new RuntimeException())
                .when(s3Client).putObject(any(PutObjectRequest.class));

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> binaryBucket.storeContent(content))
                .withMessage("Failed to store [1] with key "
                        + "[e85ca376-8d17-493f-826b-1a5a20c88e76] to bucket [testBucket]");
    }

    @Test
    public void should_propagate_exception_during_retrieval() throws Exception {
        doThrow(new RuntimeException())
                .when(s3Client).getObject(any(GetObjectRequest.class));

        assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> binaryBucket.retrieveContentFor(KEY))
                .withMessage("Failed to load object with key "
                        + "[e85ca376-8d17-493f-826b-1a5a20c88e76] from bucket [testBucket]");
    }

    @Test
    public void should_throw_illegal_argument_exception_if_key_or_byte_array_is_empty() throws Exception {
        S3Content content = newS3Content("", new byte[0], "");

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> binaryBucket.storeContent(content));
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> binaryBucket.retrieveContentFor(""));
    }

    @Test
    public void should_throw_illegal_argument_exception_if_content_is_null() throws Exception {
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> binaryBucket.storeContent(null));
    }

    @Test
    public void should_check_for_object_existence_in_bucket() throws Exception {
        when(s3Client.doesObjectExist(BUCKET_NAME, "someKey")).thenReturn(true);

        boolean objectExists = binaryBucket.exists("someKey");

        assertThat(objectExists).isTrue();
        verify(s3Client).doesObjectExist(BUCKET_NAME, "someKey");
    }
}