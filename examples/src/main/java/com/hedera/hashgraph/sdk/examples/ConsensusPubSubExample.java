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
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * How to operate with a public HCS topic.
 * <p>
 * Subscribe to the topic and publish a number of messages to the topic (no key required).
 */
class ConsensusPubSubExample {

    private static final int TOTAL_MESSAGES = 5;

    private static final CountDownLatch MESSAGES_LATCH = new CountDownLatch(TOTAL_MESSAGES);

    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Operator's account ID.
     * Used to sign and pay for operations on Hedera.
     */
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    /**
     * Operator's private key.
     */
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    /**
     * HEDERA_NETWORK defaults to testnet if not specified in dotenv file.
     * Network can be: localhost, testnet, previewnet or mainnet.
     */
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    /**
     * SDK_LOG_LEVEL defaults to SILENT if not specified in dotenv file.
     * Log levels can be: TRACE, DEBUG, INFO, WARN, ERROR, SILENT.
     * <p>
     * Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
     * for example via VM options: -Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace
     */
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Consensus Service Submit Message To The Public Topic And Subscribe Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Create new HCS topic.
         */
        System.out.println("Creating new topic...");

        TransactionResponse transactionResponse = new TopicCreateTransaction()
            .setAdminKey(operatorPublicKey)
            .execute(client);

        TransactionReceipt transactionReceipt = transactionResponse.getReceipt(client);

        TopicId topicId = Objects.requireNonNull(transactionReceipt.topicId);

        System.out.println("Created new topic with ID: " + topicId);

        /*
         * Step 2:
         * Wait 5 seconds (to ensure data propagated to mirror nodes).
         */
        System.out.println("Wait 5 seconds (to ensure data propagated to mirror nodes) ...");
        Thread.sleep(5_000);

        /*
         * Step 3:
         * Subscribe to messages on the topic, printing out the received message and metadata as it is published by the
         * Hedera mirror node.
         */
        System.out.println("Setting up a mirror client...");
        new TopicMessageQuery()
            .setTopicId(topicId)
            .subscribe(client, resp -> {
                String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);
                System.out.println("Topic message received!" +
                    " | Time: " + resp.consensusTimestamp +
                    " | Content: " + messageAsString);
                MESSAGES_LATCH.countDown();
            });

        /*
         * Step 4:
         * Publish a list of messages to a topic.
         */
        for (int i = 0; i <= TOTAL_MESSAGES; i++) {
            String message = "message #" + i;

            System.out.println("Publishing message to the topic: " + message);

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .execute(client)
                .getReceipt(client);

            Thread.sleep(2_500);
        }

        // Wait 30 seconds to receive all the messages. Fail if not received.
        boolean allMessagesReceived = MESSAGES_LATCH.await(30, TimeUnit.SECONDS);

        /*
         * Clean up:
         * Delete created topic.
         */
        new TopicDeleteTransaction()
            .setTopicId(topicId)
            .execute(client)
            .getReceipt(client);

        client.close();

        // Fail if messages weren't received.
        if (!allMessagesReceived) {
            throw new TimeoutException("Not all topic messages were received! (Fail)");
        }

        System.out.println("Consensus Service Submit Message To The Public Topic And Subscribe Example Complete!");
    }
}
