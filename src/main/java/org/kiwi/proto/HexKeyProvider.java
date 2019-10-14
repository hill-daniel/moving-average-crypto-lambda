package org.kiwi.proto;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.codec.binary.Hex.encodeHexString;

import org.kiwi.aws.s3.S3KeyProvider;

public class HexKeyProvider implements S3KeyProvider {

    private static final String SEPARATOR = "_";

    @Override
    public String createKeyFor(String id) {
        if (isNullOrEmpty(id)) {
            throw new IllegalArgumentException("Given id was empty");
        }
        try {
            return encodeHexString(id.getBytes(UTF_8)).concat(SEPARATOR).concat(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create key for [" + id + "]");
        }
    }
}
