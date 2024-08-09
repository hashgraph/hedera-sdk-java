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

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This example demonstrates sending a large message (involving `ChunkedTransaction`) to the topic and then receiving it.
 */
class ConsensusPubSubChunkedExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private static final CountDownLatch LARGE_MESSAGE_LATCH = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Generate a submit key to use with the topic.
         */
        PrivateKey submitPrivateKey = PrivateKey.generateED25519();
        PublicKey submitPublicKey = submitPrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create a new topic.
         */
        TopicId newTopicId = new TopicCreateTransaction()
            .setTopicMemo("hedera-sdk-java/ConsensusPubSubChunkedExample")
            .setAdminKey(operatorPublicKey)
            .setSubmitKey(submitPublicKey)
            .execute(client)
            .getReceipt(client)
            .topicId;

        assert newTopicId != null;

        System.out.println("for topic " + newTopicId);

        /*
         * Step 3:
         * Sleep for 10 seconds (wait to propagate to the mirror).
         */
        System.out.println("wait 10s to propagate to the mirror ...");
        Thread.sleep(10_000);

        /*
         * Step 4:
         * Set up a mirror client to print out messages as we receive them.
         */
        new TopicMessageQuery()
            .setTopicId(newTopicId)
            .subscribe(client, topicMessage -> {
                System.out.println("at " + topicMessage.consensusTimestamp + " ( seq = " + topicMessage.sequenceNumber + " ) received topic message of " + topicMessage.contents.length + " bytes");
                LARGE_MESSAGE_LATCH.countDown();
            });

        /*
         * Step 5:
         * Send message (a large one) to the topic created in previous steps.
         */
        // Get a large file to send.
        String bigContents = readResources("util/large_message.txt");

        System.out.println("about to prepare a transaction to send a message of " + bigContents.length() + " bytes");

        // Prepare a message send transaction that requires a submit key from "somewhere else".
        @Var Transaction<?> transaction = new TopicMessageSubmitTransaction()
            .setMaxChunks(15) // this is 10 by default
            .setTopicId(newTopicId)
            .setMessage(bigContents)
            // Sign with the operator or "sender" of the message,
            // this is the party who will be charged the transaction fee.
            .signWithOperator(client);

        // Serialize to bytes, so we can be signed "somewhere else" by the submit key.
        byte[] transactionBytes = transaction.toBytes();

        // Now pretend we sent those bytes across the network.
        // Parse them into a transaction, so we can sign as the submit key.
        transaction = Transaction.fromBytes(transactionBytes);

        // View out the message size from the parsed transaction.
        // This can be useful to display what we are about to sign.
        long transactionMessageSize = ((TopicMessageSubmitTransaction) transaction).getMessage().size();
        System.out.println("about to send a transaction with a message of " + transactionMessageSize + " bytes");

        // Sign with that submit key.
        transaction.sign(submitPrivateKey);

        // Now actually submit the transaction and get the receipt to ensure there were no errors.
        transaction.execute(client).getReceipt(client);

        // Wait 60 seconds to receive the message. Fail if not received.
        boolean largeMessageReceived = LARGE_MESSAGE_LATCH.await(60, TimeUnit.SECONDS);

        /*
         * Clean up:
         * Delete created topic.
         */
        new TopicDeleteTransaction()
            .setTopicId(newTopicId)
            .execute(client)
            .getReceipt(client);

        client.close();

        // Fail if message wasn't received.
        if (!largeMessageReceived) {
            throw new TimeoutException("Large topic message was not received!");
        }

        System.out.println("Example complete!");
    }

    // TODO: check if it will possible to optimize it
    private static String readResources(String filename) {
        InputStream inputStream = ConsensusPubSubChunkedExample.class.getResourceAsStream(filename);
        StringBuilder bigContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), UTF_8))) {
            @Var String line;
            while ((line = reader.readLine()) != null) {
                bigContents.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bigContents.toString();
    }
}
