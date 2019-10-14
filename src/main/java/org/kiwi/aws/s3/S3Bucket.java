package org.kiwi.aws.s3;

public interface S3Bucket {

    void storeContent(S3Content content);

    S3Content retrieveContentFor(String key);

    boolean exists(String key);
}
