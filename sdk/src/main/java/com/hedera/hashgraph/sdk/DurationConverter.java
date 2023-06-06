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

import java.time.Duration;

/**
 * Utility class used internally by the sdk.
 */
final class DurationConverter {
    private DurationConverter() {
    }

    /**
     * Create a duration object from a protobuf.
     *
     * @param duration                  the duration protobuf
     * @return                          the duration object
     */
    static Duration fromProtobuf(com.hedera.hashgraph.sdk.proto.Duration duration) {
        return Duration.ofSeconds(duration.getSeconds());
    }

    /**
     * Convert the duration object into a protobuf.
     *
     * @param duration                  the duration object
     * @return                          the protobuf
     */
    static com.hedera.hashgraph.sdk.proto.Duration toProtobuf(Duration duration) {
        return com.hedera.hashgraph.sdk.proto.Duration.newBuilder()
            .setSeconds(duration.getSeconds())
            .build();
    }
}
