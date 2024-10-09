/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
 */

package com.hedera.hashgraph.sdk;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import io.github.jsonSnapshot.SnapshotMatcher;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TransactionReceiptTest {
    static final Instant time = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start(Snapshot::asJsonString);
    }

    @AfterAll
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    static TransactionReceipt spawnReceiptExample() {
        return new TransactionReceipt(
                null,
                Status.SCHEDULE_ALREADY_DELETED,
                new ExchangeRate(3, 4, time),
                AccountId.fromString("1.2.3"),
                FileId.fromString("4.5.6"),
                ContractId.fromString("3.2.1"),
                TopicId.fromString("9.8.7"),
                TokenId.fromString("6.5.4"),
                3L,
                ByteString.copyFrom("how now brown cow", StandardCharsets.UTF_8),
                30L,
                ScheduleId.fromString("1.1.1"),
                TransactionId.withValidStart(AccountId.fromString("3.3.3"), time),
                List.of(1L, 2L, 3L),
                1,
                new ArrayList<>(),
                new ArrayList<>());
    }

    @Test
    void shouldSerialize() throws Exception {
        var originalTransactionReceipt = spawnReceiptExample();
        byte[] transactionReceiptBytes = originalTransactionReceipt.toBytes();
        var copyTransactionReceipt = TransactionReceipt.fromBytes(transactionReceiptBytes);
        assertThat(copyTransactionReceipt.toString()).isEqualTo(originalTransactionReceipt.toString());
        SnapshotMatcher.expect(originalTransactionReceipt.toString()).toMatchSnapshot();
    }
}
