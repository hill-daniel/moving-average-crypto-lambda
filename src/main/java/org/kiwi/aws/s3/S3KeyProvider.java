package org.kiwi.aws.s3;

public interface S3KeyProvider {

    String createKeyFor(String id);

}
