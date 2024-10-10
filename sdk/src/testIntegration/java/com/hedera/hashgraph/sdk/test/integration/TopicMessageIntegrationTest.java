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

import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;
import java.time.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicMessageIntegrationTest {
    @Test
    @DisplayName("Can receive a topic message")
    void canReceiveATopicMessage() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            var info = new TopicInfoQuery()
                .setTopicId(topicId)
                .execute(testEnv.client);

            assertThat(info.topicId).isEqualTo(topicId);
            assertThat(info.topicMemo).isEqualTo("[e2e::TopicCreateTransaction]");
            assertThat(info.sequenceNumber).isEqualTo(0);
            assertThat(info.adminKey).isEqualTo(testEnv.operatorKey);

            Thread.sleep(3000);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
                .setTopicId(topicId)
                .setStartTime(Instant.EPOCH)
                .subscribe(testEnv.client, (message) -> {
                    receivedMessage[0] = new String(message.contents, StandardCharsets.UTF_8).equals("Hello, from HCS!");
                });

            Thread.sleep(3000);

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("Hello, from HCS!")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            while (!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
                    throw new Exception("TopicMessage was not received in 60 seconds or less");
                }

                Thread.sleep(2000);
            }

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can receive a large topic message")
    void canReceiveALargeTopicMessage() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            // Skip if using local node.
            // Note: this check should be removed once the local node is supporting multiple nodes.
            testEnv.assumeNotLocalNode();

            var response = new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setTopicMemo("[e2e::TopicCreateTransaction]")
                .execute(testEnv.client);

            var topicId = Objects.requireNonNull(response.getReceipt(testEnv.client).topicId);

            Thread.sleep(5000);

            var info = new TopicInfoQuery()
                .setTopicId(topicId)
                    .execute(testEnv.client);

            assertThat(info.topicId).isEqualTo(topicId);
            assertThat(info.topicMemo).isEqualTo("[e2e::TopicCreateTransaction]");
            assertThat(info.sequenceNumber).isEqualTo(0);
            assertThat(info.adminKey).isEqualTo(testEnv.operatorKey);

            var receivedMessage = new boolean[]{false};
            var start = Instant.now();

            var handle = new TopicMessageQuery()
                .setTopicId(topicId)
                .setStartTime(Instant.EPOCH)
                .subscribe(testEnv.client, (message) -> {
                    receivedMessage[0] = new String(message.contents, StandardCharsets.UTF_8).equals(Contents.BIG_CONTENTS);
                });

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(Contents.BIG_CONTENTS)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            while (!receivedMessage[0]) {
                if (Duration.between(start, Instant.now()).compareTo(Duration.ofSeconds(60)) > 0) {
                    throw new Exception("TopicMessage was not received in 60 seconds or less");
                }

                Thread.sleep(1000);
            }

            new TopicDeleteTransaction()
                .setTopicId(topicId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }
}
