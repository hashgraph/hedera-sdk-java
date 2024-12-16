// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import org.hiero.sdk.TopicCreateTransaction;
import org.hiero.sdk.TopicDeleteTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TopicCreateIntegrationTest {
    @Test
    @DisplayName("Can create topic")
    void canCreateTopic() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new TopicCreateTransaction()
                    .setAdminKey(testEnv.operatorKey)
                    .setTopicMemo("[e2e::TopicCreateTransaction]")
                    .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            new TopicDeleteTransaction()
                    .setTopicId(topicId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
        }
    }

    @Test
    @DisplayName("Can create topic with no field set")
    void canCreateTopicWithNoFieldsSet() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new TopicCreateTransaction().execute(testEnv.client);
            assertThat(response.getReceipt(testEnv.client).topicId).isNotNull();
        }
    }
}
