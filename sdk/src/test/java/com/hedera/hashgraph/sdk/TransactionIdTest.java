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

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionIdTest {
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
        SnapshotMatcher.expect(TransactionId.fromString("0.0.23847@1588539964.632521325").toString()).toMatchSnapshot();
    }

    @Test
    void shouldSerialize2() {
        SnapshotMatcher.expect(TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled/3").toString()).toMatchSnapshot();
    }

    @Test
    void shouldToBytes() {
        var originalId = TransactionId.fromString("0.0.23847@1588539964.632521325");
        var copyId = TransactionId.fromProtobuf(originalId.toProtobuf());
        assertThat(copyId.toString()).isEqualTo(originalId.toString());
    }

    @Test
    void shouldToBytes2() {
        var originalId = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled/2");
        var copyId = TransactionId.fromProtobuf(originalId.toProtobuf());
        assertThat(copyId.toString()).isEqualTo(originalId.toString());
    }

    @Test
    void shouldParse() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertThat(accountId.shard).isEqualTo(0);
        assertThat(accountId.num).isEqualTo(23847);
        assertThat(validStart.getEpochSecond()).isEqualTo(1588539964);
        assertThat(validStart.getNano()).isEqualTo(632521325);
    }

    @Test
    void shouldParseScheduled() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertThat(accountId.shard).isEqualTo(0);
        assertThat(accountId.num).isEqualTo(23847);
        assertThat(validStart.getEpochSecond()).isEqualTo(1588539964);
        assertThat(validStart.getNano()).isEqualTo(632521325);
        assertThat(transactionId.getScheduled()).isTrue();
        assertThat(transactionId.getNonce()).isNull();

        assertThat(transactionId.toString()).isEqualTo("0.0.23847@1588539964.632521325?scheduled");
    }

    @Test
    void shouldParseNonce() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325/4");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertThat(accountId.shard).isEqualTo(0);
        assertThat(accountId.num).isEqualTo(23847);
        assertThat(validStart.getEpochSecond()).isEqualTo(1588539964);
        assertThat(validStart.getNano()).isEqualTo(632521325);
        assertThat(transactionId.getScheduled()).isFalse();
        assertThat(transactionId.getNonce()).isEqualTo(4);

        assertThat(transactionId.toString()).isEqualTo("0.0.23847@1588539964.632521325/4");
    }

    @Test
    void compare() {
        // Compare when only one of the txs is schedules
        var transactionId1 = TransactionId.fromString("0.0.23847@1588539964.632521325");
        var transactionId2 = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled");
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(-1);

        transactionId1 = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled");
        transactionId2 = TransactionId.fromString("0.0.23847@1588539964.632521325");
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(1);

        // Compare when only one of the txs has accountId
        transactionId1 = new TransactionId(null, Instant.ofEpochSecond(1588539964));
        transactionId2 = new TransactionId(AccountId.fromString("0.0.23847"), Instant.ofEpochSecond(1588539964));
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(-1);

        transactionId1 = new TransactionId(AccountId.fromString("0.0.23847"), Instant.ofEpochSecond(1588539964));
        transactionId2 = new TransactionId(null, Instant.ofEpochSecond(1588539964));
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(1);

        // Compare the AccountIds
        transactionId1 = TransactionId.fromString("0.0.23847@1588539964.632521325");
        transactionId2 = TransactionId.fromString("0.0.23847@1588539964.632521325");
        assertThat(transactionId1).isEqualByComparingTo(transactionId2);

        transactionId1 = TransactionId.fromString("0.0.23848@1588539964.632521325");
        transactionId2 = TransactionId.fromString("0.0.23847@1588539964.632521325");
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(1);

        transactionId1 = TransactionId.fromString("0.0.23847@1588539964.632521325");
        transactionId2 = TransactionId.fromString("0.0.23848@1588539964.632521325");
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(-1);

        // Compare when only one of the txs has valid start
        transactionId1 = new TransactionId(null, null);
        transactionId2 = new TransactionId(null, Instant.ofEpochSecond(1588539964));
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(-1);

        transactionId1 = new TransactionId(AccountId.fromString("0.0.23847"), Instant.ofEpochSecond(1588539964));
        transactionId2 = new TransactionId(null, null);
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(1);

        // Compare the validStarts
        transactionId1 = new TransactionId(null, Instant.ofEpochSecond(1588539965));
        transactionId2 = new TransactionId(null, Instant.ofEpochSecond(1588539964));
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(1);

        transactionId1 = new TransactionId(null, Instant.ofEpochSecond(1588539964));
        transactionId2 = new TransactionId(null, Instant.ofEpochSecond(1588539965));
        assertThat(transactionId1.compareTo(transactionId2)).isEqualTo(-1);


        transactionId1 = new TransactionId(null, null);
        transactionId2 = new TransactionId(null, null);
        assertThat(transactionId1).isEqualByComparingTo(transactionId2);
    }
}
