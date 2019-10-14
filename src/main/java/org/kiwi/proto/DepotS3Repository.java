package org.kiwi.proto;

import static org.kiwi.aws.s3.S3Content.newS3Content;

import com.google.inject.Inject;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Arrays;
import java.util.Optional;
import org.kiwi.aws.s3.S3Bucket;
import org.kiwi.aws.s3.S3Content;
import org.kiwi.aws.s3.S3KeyProvider;
import org.kiwi.proto.FloatingAverageProtos.Depot;

public class DepotS3Repository implements DepotRepository {

    private static final String CONTENT_TYPE = "binary/octet-stream";

    private final S3Bucket s3Bucket;
    private final S3KeyProvider keyProvider;

    @Inject
    DepotS3Repository(S3Bucket s3Bucket, S3KeyProvider keyProvider) {
        this.s3Bucket = s3Bucket;
        this.keyProvider = keyProvider;
    }

    @Override
    public Optional<Depot> load(String id) {
        String key = keyProvider.createKeyFor(id);
        if (s3Bucket.exists(key)) {
            Depot depot = loadDepotFor(key);
            return Optional.of(depot);
        }
        return Optional.empty();
    }

    @Override
    public void store(Depot depot) {
        S3Content content = createS3ContentOf(depot);
        s3Bucket.storeContent(content);
    }

    private Depot loadDepotFor(String key) {
        byte[] bytes = s3Bucket.retrieveContentFor(key).bytes();
        return toDepot(bytes);
    }

    private Depot toDepot(byte[] bytes) {
        try {
            return Depot.parseFrom(bytes);
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Failed to parse to Depot from " + Arrays.toString(bytes));
        }
    }

    private S3Content createS3ContentOf(Depot depot) {
        String key = keyProvider.createKeyFor(depot.getId());
        byte[] bytes = depot.toByteArray();
        return createS3ContentWith(key, bytes);
    }

    private S3Content createS3ContentWith(String key, byte[] bytes) {
        return newS3Content(key, bytes, CONTENT_TYPE);
    }
}
