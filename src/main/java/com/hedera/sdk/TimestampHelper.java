package com.hedera.sdk;

import com.hedera.sdk.proto.Timestamp;
import com.hedera.sdk.proto.TimestampSeconds;
import java.time.Instant;

final class TimestampHelper {
    private TimestampHelper() {}

    static Timestamp timestampFrom(Instant timestamp) {
        return Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();
    }

    static TimestampSeconds timestampSecondsFrom(Instant timestamp) {
        return TimestampSeconds.newBuilder().setSeconds(timestamp.getEpochSecond()).build();
    }
}
