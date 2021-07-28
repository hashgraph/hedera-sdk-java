package com.hedera.hashgraph.sdk;

public class InvalidChecksumException extends Exception {
    public final long shard;
    public final long realm;
    public final long num;
    public final String presentChecksum;
    public final String expectedChecksum;

    InvalidChecksumException(long shard, long realm, long num, String presentChecksum, String expectedChecksum) {
        this.shard = shard;
        this.realm = realm;
        this.num = num;
        this.presentChecksum = presentChecksum;
        this.expectedChecksum = expectedChecksum;
    }

    @Override
    public String getMessage() {
        return "Invalid checksum for entity ID \"" + shard + "." + realm + "." + num +
            "\": expected \"" + expectedChecksum + "\", but provided checksum was \"" + presentChecksum + "\"";
    }
}
