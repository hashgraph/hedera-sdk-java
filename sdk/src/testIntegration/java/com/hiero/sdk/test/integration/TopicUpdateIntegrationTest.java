/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2021 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk.test.integration;

import com.hiero.sdk.TopicCreateTransaction;
import com.hiero.sdk.TopicDeleteTransaction;
import com.hiero.sdk.TopicInfoQuery;
import com.hiero.sdk.TopicUpdateTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicUpdateIntegrationTest {
    @Test
    @DisplayName("Can update topic")
    void canUpdateTopic() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setAutoRenewAccountId(testEnv.operatorId)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            new TopicUpdateTransaction()
                .clearAutoRenewAccountId()
                .setTopicMemo("hello")
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var topicInfo = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertThat(topicInfo.topicMemo).isEqualTo("hello");
            assertThat(topicInfo.autoRenewAccountId).isNull();

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }
}
