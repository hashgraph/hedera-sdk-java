package com.hedera.sdk;

import java.time.Instant;
import javax.annotation.Nonnull;

public class TimestampSeconds {
    com.hedera.sdk.proto.TimestampSeconds.Builder inner;

    public TimestampSeconds(@Nonnull Instant timestamp) {
        inner =
                com.hedera.sdk.proto.TimestampSeconds.newBuilder()
                        .setSeconds(timestamp.getEpochSecond());
    }
}
