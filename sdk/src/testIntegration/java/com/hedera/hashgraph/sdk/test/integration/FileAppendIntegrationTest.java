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
package com.hedera.hashgraph.sdk.test.integration;

import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.KeyList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class FileAppendIntegrationTest {
    @Test
    @DisplayName("Can append to file")
    void canAppendToFile() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        try(var testEnv = new IntegrationTestEnv(1)){

            var response = new FileCreateTransaction()
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(28);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNotNull();
            assertThat(info.keys.getThreshold()).isNull();
            assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

            new FileAppendTransaction()
                .setFileId(fileId)
                .setContents("[e2e::FileAppendTransaction]")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(56);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNotNull();
            assertThat(info.keys.getThreshold()).isNull();
            assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can append large contents to file")
    void canAppendLargeContentsToFile() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        try(var testEnv = new IntegrationTestEnv(2)){

            // Skip if using local node.
            // Note: this check should be removed once the local node is supporting multiple nodes.
            testEnv.assumeNotLocalNode();

            var response = new FileCreateTransaction()
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            Thread.sleep(5000);

            var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(28);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNotNull();
            assertThat(info.keys.getThreshold()).isNull();
            assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

            new FileAppendTransaction()
                .setFileId(fileId)
                .setContents(Contents.BIG_CONTENTS)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var contents = new FileContentsQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertThat(contents.toStringUtf8()).isEqualTo("[e2e::FileCreateTransaction]" + Contents.BIG_CONTENTS);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(13522);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNotNull();
            assertThat(info.keys.getThreshold()).isNull();
            assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can append large contents to file despite TRANSACTION_EXPIRATION response codes")
    void canAppendLargeContentsToFileDespiteExpiration() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        try(var testEnv = new IntegrationTestEnv(2)){

            // Skip if using local node.
            // Note: this check should be removed once the local node is supporting multiple nodes.
            testEnv.assumeNotLocalNode();

            var response = new FileCreateTransaction()
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            Thread.sleep(5000);

            var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(28);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNotNull();
            assertThat(info.keys.getThreshold()).isNull();
            assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

            var appendTx = new FileAppendTransaction()
                .setFileId(fileId)
                .setContents(Contents.BIG_CONTENTS)
                .setTransactionValidDuration(Duration.ofSeconds(25))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var contents = new FileContentsQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertThat(contents.toStringUtf8()).isEqualTo("[e2e::FileCreateTransaction]" + Contents.BIG_CONTENTS);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertThat(info.fileId).isEqualTo(fileId);
            assertThat(info.size).isEqualTo(13522);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.keys).isNotNull();
            assertThat(info.keys.getThreshold()).isNull();
            assertThat(info.keys).isEqualTo(KeyList.of(testEnv.operatorKey));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }
}
