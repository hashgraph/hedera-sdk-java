// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiero.sdk.TopicCreateTransaction;
import com.hiero.sdk.TopicDeleteTransaction;
import com.hiero.sdk.TopicInfoQuery;
import com.hiero.sdk.TopicUpdateTransaction;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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

            var topicInfo = new TopicInfoQuery().setTopicId(topicId).execute(testEnv.client);

            assertThat(topicInfo.topicMemo).isEqualTo("hello");
            assertThat(topicInfo.autoRenewAccountId).isNull();

            new TopicDeleteTransaction()
                    .setTopicId(topicId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
        }
    }
}
