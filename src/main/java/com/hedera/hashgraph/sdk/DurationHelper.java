package com.hedera.hashgraph.sdk;

import com.hederahashgraph.api.proto.java.Duration;

public final class DurationHelper {
    private DurationHelper() { }

    public static Duration durationFrom(java.time.Duration duration) {
        return Duration.newBuilder()
            .setSeconds(duration.getSeconds())
            .build();
    }

    public static java.time.Duration durationTo(Duration duration) {
        return java.time.Duration.ofSeconds(duration.getSeconds());
    }
}
