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

import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicDeleteTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class TopicDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete topic")
    void canDeleteTopic() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setTopicMemo("[e2e::TopicCreateTransaction]")
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot delete immutable topic")
    void cannotDeleteImmutableTopic() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new TopicCreateTransaction()
            .execute(testEnv.client);

        var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.UNAUTHORIZED.toString());

        testEnv.close();
    }
}
