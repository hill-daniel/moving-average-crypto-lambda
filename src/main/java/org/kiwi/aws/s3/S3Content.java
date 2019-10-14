package org.kiwi.aws.s3;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class S3Content {

    public static S3Content newS3Content(String key, byte[] bytes, String contentType) {
        return new AutoValue_S3Content(key, bytes, contentType);
    }

    public abstract String key();

    public abstract byte[] bytes();

    public abstract String contentType();

}
