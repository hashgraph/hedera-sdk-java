package com.hedera.hashgraph.sdk;

import org.threeten.bp.Duration;

/**
 * Utility class used internally by the sdk.
 */
final class DurationConverter {
    private DurationConverter() {
    }

    /**
     * Create a duration object from a protobuf.
     *
     * @param duration                  the duration protobuf
     * @return                          the duration object
     */
    static Duration fromProtobuf(com.hedera.hashgraph.sdk.proto.Duration duration) {
        return Duration.ofSeconds(duration.getSeconds());
    }

    /**
     * Convert the duration object into a protobuf.
     *
     * @param duration                  the duration object
     * @return                          the protobuf
     */
    static com.hedera.hashgraph.sdk.proto.Duration toProtobuf(Duration duration) {
        return com.hedera.hashgraph.sdk.proto.Duration.newBuilder()
            .setSeconds(duration.getSeconds())
            .build();
    }
}
