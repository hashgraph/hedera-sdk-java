package com.hedera.hashgraph.sdk;

import com.hederahashgraph.api.proto.java.Timestamp;
import com.hederahashgraph.api.proto.java.TimestampSeconds;

import java.time.Instant;

public final class TimestampHelper {
    private TimestampHelper() { }

    public static Timestamp timestampFrom(Instant timestamp) {
        return Timestamp.newBuilder()
            .setSeconds(timestamp.getEpochSecond())
            .setNanos(timestamp.getNano())
            .build();
    }

    public static TimestampSeconds timestampSecondsFrom(Instant timestamp) {
        return TimestampSeconds.newBuilder()
            .setSeconds(timestamp.getEpochSecond())
            .build();
    }

    public static Instant timestampTo(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }
}
