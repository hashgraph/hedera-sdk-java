package com.hedera.sdk;

import com.hedera.sdk.proto.Duration;

final class DurationHelper {
    private DurationHelper() {}

    static Duration durationFrom(java.time.Duration duration) {
        return Duration.newBuilder()
                .setSeconds(duration.getSeconds())
                .setNanos(duration.getNano())
                .build();
    }
}
