package org.kiwi.aws.s3;

import static com.amazonaws.util.Md5Utils.md5AsBase64;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.ByteStreams.toByteArray;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class BinaryBucket implements S3Bucket {

    private final AmazonS3 s3Client;
    private final String bucketName;

    @Inject
    public BinaryBucket(AmazonS3 amazonS3Client, @Named("aws.s3.bucket.name") String bucketName) {
        this.s3Client = amazonS3Client;
        this.bucketName = bucketName;
    }

    @Override
    public void storeContent(S3Content content) {
        if (content == null || isNullOrEmpty(content.key()) || content.bytes() == null || content.bytes().length == 0) {
            throw new IllegalArgumentException("Invalid payload");
        }
        try {
            storeToBucket(content);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store " + Arrays.toString(content.bytes()) + " "
                    + "with key [" + content.key() + "] to bucket [" + bucketName + "]", e);
        }
    }

    @Override
    public S3Content retrieveContentFor(String key) {
        if (isNullOrEmpty(key)) {
            throw new IllegalArgumentException("Key must not be null or empty");
        }
        try {
            return retrieveWith(key);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load object with key [" + key + "] "
                    + "from bucket [" + bucketName + "]", e);
        }
    }

    @Override
    public boolean exists(String key) {
        return s3Client.doesObjectExist(bucketName, key);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{bucketName=" + bucketName + ", s3Client=" + s3Client + "}";
    }

    private void storeToBucket(S3Content content) throws IOException {
        PutObjectRequest putObjectRequest = createPutObjectRequestWith(content);
        s3Client.putObject(putObjectRequest);
    }

    private PutObjectRequest createPutObjectRequestWith(S3Content content) throws IOException {
        ObjectMetadata metaData = createMetaDataOf(content);
        try (InputStream inputStream = new ByteArrayInputStream(content.bytes())) {
            return new PutObjectRequest(bucketName, content.key(), inputStream, metaData);
        }
    }

    private S3Content retrieveWith(String key) throws IOException {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        S3Object s3Object = s3Client.getObject(getObjectRequest);
        byte[] bytes = getBytesOf(s3Object);
        String contentType = s3Object.getObjectMetadata().getContentType();
        return S3Content.newS3Content(key, bytes, contentType);
    }

    private byte[] getBytesOf(S3Object s3Object) throws IOException {
        try (InputStream inputStream = s3Object.getObjectContent()) {
            return toByteArray(inputStream);
        }
    }

    private static ObjectMetadata createMetaDataOf(S3Content content) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(content.contentType());
        objectMetadata.setContentLength(content.bytes().length);
        objectMetadata.setContentMD5(md5AsBase64(content.bytes()));
        return objectMetadata;
    }
}
