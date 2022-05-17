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

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class FeeSchedulesTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    FeeSchedules spawnFeeSchedulesExample() {
        return new FeeSchedules()
            .setCurrent(new FeeSchedule()
                .setExpirationTime(Instant.ofEpochSecond(1554158542))
                .addTransactionFeeSchedule(new TransactionFeeSchedule()
                    .addFee(new FeeData()
                        .setNodeData(new FeeComponents())
                        .setNetworkData(new FeeComponents()
                            .setMin(2)
                            .setMax(5)
                        )
                        .setServiceData(new FeeComponents())
                    )
                )
            )
            .setNext(new FeeSchedule()
                .setExpirationTime(Instant.ofEpochSecond(1554158222))
                .addTransactionFeeSchedule(new TransactionFeeSchedule()
                    .addFee(new FeeData()
                        .setNodeData(new FeeComponents()
                            .setMin(1)
                            .setMax(2)
                        )
                        .setNetworkData(new FeeComponents())
                        .setServiceData(new FeeComponents())
                    )
                )
            );
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalFeeSchedules = spawnFeeSchedulesExample();
        byte[] feeSchedulesBytes = originalFeeSchedules.toBytes();
        var copyFeeSchedules = FeeSchedules.fromBytes(feeSchedulesBytes);
        assertThat(originalFeeSchedules.toString().equals(copyFeeSchedules.toString())).isTrue();
        SnapshotMatcher.expect(originalFeeSchedules.toString()).toMatchSnapshot();
    }

    @Test
    void shouldSerializeNull() throws Exception {
        var originalFeeSchedules = new FeeSchedules();
        byte[] feeSchedulesBytes = originalFeeSchedules.toBytes();
        var copyFeeSchedules = FeeSchedules.fromBytes(feeSchedulesBytes);
        assertThat(originalFeeSchedules.toString().equals(copyFeeSchedules.toString())).isTrue();
    }
}
