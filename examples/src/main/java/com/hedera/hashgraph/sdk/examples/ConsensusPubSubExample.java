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
package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class ConsensusPubSubExample {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private static final int TOTAL_MESSAGES = 5;
    private static final CountDownLatch messagesLatch = new CountDownLatch(TOTAL_MESSAGES);

    private ConsensusPubSubExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        TransactionResponse transactionResponse = new TopicCreateTransaction()
            .execute(client);

        TransactionReceipt transactionReceipt = transactionResponse.getReceipt(client);

        TopicId topicId = Objects.requireNonNull(transactionReceipt.topicId);

        System.out.println("New topic created: " + topicId);

        Thread.sleep(5000);

        new TopicMessageQuery()
            .setTopicId(topicId)
            .subscribe(client, resp -> {
                String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);

                System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
                messagesLatch.countDown();
            });

        for (int i = 0; i <= TOTAL_MESSAGES; i++) {
            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("hello, HCS! " + i)
                .execute(client)
                .getReceipt(client);

            Thread.sleep(2_500);
        }

        boolean allMessagesReceived = messagesLatch.await(30, TimeUnit.SECONDS);

        client.close();

        if (!allMessagesReceived) {
            throw new TimeoutException("Not all topic messages were received!");
        }
    }
}
