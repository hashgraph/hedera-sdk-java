package com.hedera.sdk;

import com.hedera.sdk.proto.Timestamp;
import com.hedera.sdk.proto.TimestampSeconds;
import java.time.Instant;
import javax.annotation.Nonnull;

class TimestampHelper {
    static Timestamp timestampFrom(@Nonnull Instant timestamp) {
        return Timestamp.newBuilder()
                .setSeconds(timestamp.getEpochSecond())
                .setNanos(timestamp.getNano())
                .build();
    }

    static TimestampSeconds timestampSecondsFrom(@Nonnull Instant timestamp) {
        return TimestampSeconds.newBuilder().setSeconds(timestamp.getEpochSecond()).build();
    }
}
