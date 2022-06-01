/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Timestamp;
import com.hedera.hashgraph.sdk.proto.TimestampSeconds;

import java.time.Instant;

/**
 * Instance in time utilities.
 */
final class InstantConverter {
    /**
     * Constructor.
     */
    private InstantConverter() {
    }

    /**
     * Create an instance from a timestamp protobuf.
     *
     * @param timestamp                 the protobuf
     * @return                          the instance
     */
    static Instant fromProtobuf(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    /**
     * Create an instance from a timestamp in seconds protobuf.
     *
     * @param timestampSeconds          the protobuf
     * @return                          the instance
     */
    static Instant fromProtobuf(TimestampSeconds timestampSeconds) {
        return Instant.ofEpochSecond(timestampSeconds.getSeconds());
    }

    /**
     * Convert an instance into a timestamp.
     *
     * @param instant                   the instance
     * @return                          the timestamp
     */
    static Timestamp toProtobuf(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    /**
     * Convert an instance into a timestamp in seconds.
     *
     * @param instant                   the instance
     * @return                          the timestamp in seconds
     */
    static TimestampSeconds toSecondsProtobuf(Instant instant) {
        return TimestampSeconds.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .build();
    }
}
