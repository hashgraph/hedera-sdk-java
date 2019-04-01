package com.hedera.sdk;

import com.hedera.sdk.proto.Timestamp;
import com.hedera.sdk.proto.TimestampSeconds;
import java.time.Instant;

public final class TimestampHelper {
    private TimestampHelper() {}

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
}
