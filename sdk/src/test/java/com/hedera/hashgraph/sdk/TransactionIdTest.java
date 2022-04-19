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

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals(originalId.toString(), copyId.toString());
    }

    @Test
    void shouldToBytes2() {
        var originalId = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled/2");
        var copyId = TransactionId.fromProtobuf(originalId.toProtobuf());
        assertEquals(originalId.toString(), copyId.toString());
    }

    @Test
    void shouldParse() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertEquals(0, accountId.shard);
        assertEquals(23847, accountId.num);
        assertEquals(1588539964, validStart.getEpochSecond());
        assertEquals(632521325, validStart.getNano());
    }

    @Test
    void shouldParseScheduled() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325?scheduled");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertEquals(0, accountId.shard);
        assertEquals(23847, accountId.num);
        assertEquals(1588539964, validStart.getEpochSecond());
        assertEquals(632521325, validStart.getNano());
        assertTrue(transactionId.getScheduled());
        assertNull(transactionId.getNonce());

        assertEquals(transactionId.toString(), "0.0.23847@1588539964.632521325?scheduled");
    }

    @Test
    void shouldParseNonce() {
        var transactionId = TransactionId.fromString("0.0.23847@1588539964.632521325/4");
        var accountId = Objects.requireNonNull(transactionId.accountId);
        var validStart = Objects.requireNonNull(transactionId.validStart);
        assertEquals(0, accountId.shard);
        assertEquals(23847, accountId.num);
        assertEquals(1588539964, validStart.getEpochSecond());
        assertEquals(632521325, validStart.getNano());
        assertFalse(transactionId.getScheduled());
        assertEquals(4, transactionId.getNonce());

        assertEquals(transactionId.toString(), "0.0.23847@1588539964.632521325/4");
    }
}
