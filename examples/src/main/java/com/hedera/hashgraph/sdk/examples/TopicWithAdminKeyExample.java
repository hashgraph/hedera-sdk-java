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

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

/**
 * An example of HCS topic management using a threshold key as the adminKey and going through a key rotation to a new
 * set of keys.
 * <p>
 * Creates a new HCS topic with a 2-of-3 threshold key for the adminKey.
 * Updates the HCS topic to a 3-of-4 threshold key for the adminKey.
 */
class TopicWithAdminKeyExample {

    // See `.env.sample` in the `examples` folder root for how to specify values below
    // or set environment variables with the same names.

    // Operator's account ID.
    // Used to sign and pay for operations on Hedera.
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    // Operator's private key.
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // `HEDERA_NETWORK` defaults to `testnet` if not specified in dotenv file
    // Networks can be: `localhost`, `testnet`, `previewnet`, `mainnet`.
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    // `SDK_LOG_LEVEL` defaults to `SILENT` if not specified in dotenv file
    // Log levels can be: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `SILENT`.
    // Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
    // for example via VM options: `-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace`
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Generate the initial keys that are part of the adminKey's thresholdKey.
         * Three ED25519 keys part of a 2-of-3 threshold key.
         */
        PrivateKey[] initialAdminPrivateKeys = new PrivateKey[3];
        PublicKey[] initialAdminPublicKeys = new PublicKey[3];
        Arrays.setAll(initialAdminPrivateKeys, i -> PrivateKey.generate());
        Arrays.setAll(initialAdminPublicKeys, i -> initialAdminPrivateKeys[i].getPublicKey());

        /*
         * Step 2:
         * Create the threshold key.
         */
        KeyList thresholdKey = KeyList.withThreshold(2);
        Collections.addAll(thresholdKey, initialAdminPublicKeys);

        /*
         * Step 3:
         * Create the topic create transaction with threshold key.
         */
        Transaction<?> topicCreateTransaction = new TopicCreateTransaction()
            .setTopicMemo("demo topic")
            .setAdminKey(thresholdKey)
            .freezeWith(client);

        /*
         * Step 4:
         * Sign the topic create transaction with 2 of 3 keys that are part of the adminKey threshold key.
         */
        Arrays.stream(initialAdminPrivateKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicCreateTransaction with key " + k);
            topicCreateTransaction.sign(k);
        });

        /*
         * Step 5:
         * Execute the topic create transaction.
         */
        TransactionResponse transactionResponse = topicCreateTransaction.execute(client);
        TopicId topicId = transactionResponse.getReceipt(client).topicId;
        System.out.println("Created new topic " + topicId + " with 2-of-3 threshold key as adminKey.");

        /*
         * Step 6:
         * Generate the new keys that are part of the adminKey's thresholdKey.
         * Four ED25519 keys part of a 3-of-4 threshold key.
         */
        PrivateKey[] newAdminKeys = new PrivateKey[4];
        PublicKey[] newAdminPublicKeys = new PublicKey[4];
        Arrays.setAll(newAdminKeys, i -> PrivateKey.generate());
        Arrays.setAll(newAdminPublicKeys, i -> newAdminKeys[i].getPublicKey());

        /*
         * Step 7:
         * Create the new threshold key.
         */
        KeyList newThresholdKey = KeyList.withThreshold(3);
        Collections.addAll(newThresholdKey, newAdminPublicKeys);

        /*
         * Step 8:
         * Create the topic update transaction with the new threshold key.
         */
        Transaction<?> topicUpdatetransaction = new TopicUpdateTransaction()
            .setTopicId(topicId)
            .setTopicMemo("updated demo topic")
            .setAdminKey(newThresholdKey)
            .freezeWith(client);

        /*
         * Step 9:
         * Sign the topic update transaction with the initial adminKey.
         * 2 of the 3 keys already part of the topic's adminKey.
         */
        Arrays.stream(initialAdminPrivateKeys, 0, 2).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with initial admin key " + k);
            topicUpdatetransaction.sign(k);
        });

        /*
         * Step 9:
         * Sign the topic update transaction with the new adminKey.
         * 3 of 4 keys already part of the topic's adminKey.
         */
        Arrays.stream(newAdminKeys, 0, 3).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with new admin key " + k);
            topicUpdatetransaction.sign(k);
        });

        /*
         * Step 10:
         * Execute the topic update transaction.
         */
        TransactionResponse transactionResponse2 = topicUpdatetransaction.execute(client);

        // Retrieve results post-consensus.
        transactionResponse2.getReceipt(client);
        System.out.println("Updated topic " + topicId + " with 3-of-4 threshold key as adminKey");

        /*
         * Step 11:
         * Query the topic info and output it.
         */
        TopicInfo topicInfo = new TopicInfoQuery()
            .setTopicId(topicId)
            .execute(client);
        System.out.println(topicInfo);

        /*
         * Clean up:
         * Delete created topic.
         */
        var topicDeleteTransaction = new TopicDeleteTransaction()
            .setTopicId(topicId)
            .freezeWith(client);

        Arrays.stream(newAdminKeys, 0, 3).forEach(k -> {
            System.out.println("Signing ConsensusTopicUpdateTransaction with new admin key " + k);
            topicDeleteTransaction.sign(k);
        });

        topicDeleteTransaction.execute(client).getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
