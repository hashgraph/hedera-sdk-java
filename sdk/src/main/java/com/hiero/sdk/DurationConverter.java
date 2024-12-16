// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import java.time.Duration;

/**
 * Utility class used internally by the sdk.
 */
final class DurationConverter {
    private DurationConverter() {}

    /**
     * Create a duration object from a protobuf.
     *
     * @param duration                  the duration protobuf
     * @return                          the duration object
     */
    static Duration fromProtobuf(com.hiero.sdk.proto.Duration duration) {
        return Duration.ofSeconds(duration.getSeconds());
    }

    /**
     * Convert the duration object into a protobuf.
     *
     * @param duration                  the duration object
     * @return                          the protobuf
     */
    static com.hiero.sdk.proto.Duration toProtobuf(Duration duration) {
        return com.hiero.sdk.proto.Duration.newBuilder()
                .setSeconds(duration.getSeconds())
                .build();
    }
}
