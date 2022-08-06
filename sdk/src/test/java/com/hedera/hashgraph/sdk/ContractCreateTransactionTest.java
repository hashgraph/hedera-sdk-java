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
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class ContractCreateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldSerialize2() {
        SnapshotMatcher.expect(spawnTestTransaction2()
            .toString()
        ).toMatchSnapshot();
    }

    private ContractCreateTransaction spawnTestTransaction() {
        return new ContractCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setBytecodeFileId(FileId.fromString("0.0.3003"))
            .setAdminKey(unusedPrivateKey)
            .setGas(0)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .setStakedAccountId(AccountId.fromString("0.0.3"))
            .setMaxAutomaticTokenAssociations(101)
            .setAutoRenewPeriod(Duration.ofHours(10))
            .setConstructorParameters(new byte[]{10, 11, 12, 13, 25})
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .setAutoRenewAccountId(new AccountId(30))
            .freeze()
            .sign(unusedPrivateKey);
    }

    private ContractCreateTransaction spawnTestTransaction2() {
        return new ContractCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setBytecode(Hex.decode("deadbeef"))
            .setAdminKey(unusedPrivateKey)
            .setGas(0)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .setStakedNodeId(4L)
            .setMaxAutomaticTokenAssociations(101)
            .setAutoRenewPeriod(Duration.ofHours(10))
            .setConstructorParameters(new byte[]{10, 11, 12, 13, 25})
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .setAutoRenewAccountId(new AccountId(30))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = ContractCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx.toString());
    }

    @Test
    void shouldBytes2() throws Exception {
        var tx = spawnTestTransaction2();
        var tx2 = ContractCreateTransaction.fromBytes(tx.toBytes());
        assertThat(tx2.toString()).isEqualTo(tx2.toString());
    }
}
