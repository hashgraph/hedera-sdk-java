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
import java.time.Instant;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An example of an HCS topic that utilizes a submitKey to limit who can submit messages on the topic.
 * <p>
 * Creates a new HCS topic with a single ED25519 submitKey.
 * Subscribes to the topic (no key required).
 * Publishes a number of messages to the topic signed by the submitKey.
 */
class ConsensusPubSubWithSubmitKeyExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private static final int TOTAL_MESSAGES = 5;

    private static final CountDownLatch MESSAGES_LATCH = new CountDownLatch(TOTAL_MESSAGES);

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        PublicKey operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Generate a brand new ED25519 key pair.
         * Create a new topic with that key as the topic's submitKey; required to sign all future
         * ConsensusMessageSubmitTransactions for that topic.
         */
        // Generate a Ed25519 private, public key pair
        PrivateKey submitPrivateKey = PrivateKey.generateED25519();
        PublicKey submitPublicKey = submitPrivateKey.getPublicKey();

        TransactionResponse transactionResponse = new TopicCreateTransaction()
            .setTopicMemo("HCS topic with submit key")
            .setAdminKey(operatorPublicKey)
            .setSubmitKey(submitPublicKey)
            .execute(client);

        TopicId topicId = Objects.requireNonNull(transactionResponse.getReceipt(client).topicId);
        System.out.println("Created new topic " + topicId + " with ED25519 submitKey of " + submitPrivateKey);

        /*
         * Step 2:
         * Sleep for 5 seconds (wait to propagate to the mirror).
         */
        Thread.sleep(5_000);

        /*
         * Step 3:
         * Subscribe to messages on the topic, printing out the received message and metadata as it is published by the
         * Hedera mirror node.
         */
        new TopicMessageQuery()
            .setTopicId(topicId)
            .setStartTime(Instant.ofEpochSecond(0))
            .subscribe(client, (resp) -> {
                String messageAsString = new String(resp.contents, StandardCharsets.UTF_8);

                System.out.println(resp.consensusTimestamp + " received topic message: " + messageAsString);
                MESSAGES_LATCH.countDown();
            });

        /*
         * Step 4:
         * Publish a list of messages to a topic, signing each transaction with the topic's submitKey.
         */
        Random r = new Random();
        for (int i = 0; i < TOTAL_MESSAGES; i++) {
            String message = "random message " + r.nextLong();

            System.out.println("Publishing message: " + message);

            new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage(message)
                .freezeWith(client)

                // The transaction is automatically signed by the payer.
                // Due to the topic having a submitKey requirement, additionally sign the transaction with that key.
                .sign(submitPrivateKey)

                .execute(client)
                .transactionId
                .getReceipt(client);

            Thread.sleep(2_000);
        }

        // Wait 60 seconds to receive all the messages. Fail if not received.
        boolean allMessagesReceived = MESSAGES_LATCH.await(180, TimeUnit.SECONDS);

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
            throw new TimeoutException("Not all topic messages were received!");
        }

        System.out.println("Example complete!");
    }
}
