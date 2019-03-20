package com.hedera.sdk;

import java.time.Instant;
import javax.annotation.Nonnull;

public class Timestamp {
    com.hedera.sdk.proto.Timestamp.Builder inner;

    public Timestamp(@Nonnull Instant timestamp) {
        inner =
                com.hedera.sdk.proto.Timestamp.newBuilder()
                        .setSeconds(timestamp.getEpochSecond())
                        .setNanos(timestamp.getNano());
    }
}
