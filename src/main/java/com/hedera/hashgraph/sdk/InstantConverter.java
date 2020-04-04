package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Timestamp;
import org.threeten.bp.Instant;

final class InstantConverter {
    private InstantConverter() {
    }

    static Timestamp toProtobuf(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }
}
