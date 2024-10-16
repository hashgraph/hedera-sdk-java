/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ScheduleCreateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }


    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    private ScheduleCreateTransaction spawnTestTransaction() {
        var transferTransaction = new TransferTransaction()
            .addHbarTransfer(AccountId.fromString("0.0.555"), new Hbar(-10))
            .addHbarTransfer(AccountId.fromString("0.0.333"), new Hbar(10));
        return transferTransaction.schedule()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setAdminKey(unusedPrivateKey)
            .setPayerAccountId(AccountId.fromString("0.0.222"))
            .setScheduleMemo("hi")
            .setMaxTransactionFee(new Hbar(1))
            .setExpirationTime(validStart)
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = ScheduleCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }
}
