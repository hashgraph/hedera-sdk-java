package com.hedera.hashgraph.sdk;

import org.threeten.bp.Duration;

final class DurationConverter {
    private DurationConverter() {}

    static com.hedera.hashgraph.sdk.proto.Duration toProtobuf(Duration duration) {
        return com.hedera.hashgraph.sdk.proto.Duration.newBuilder()
                .setSeconds(duration.getSeconds())
                .build();
    }
}
