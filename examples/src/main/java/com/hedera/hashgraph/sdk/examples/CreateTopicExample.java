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

import java.util.Objects;

/**
 * Create a public topic and submit a message to it.
 */
class CreateTopicExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

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
         * Create a new topic.
         */
        TransactionResponse createResponse = new TopicCreateTransaction()
            .setAdminKey(operatorPublicKey)
            .execute(client);

        TransactionReceipt createReceipt = createResponse.getReceipt(client);
        System.out.println("topic id = " + createReceipt.topicId);

        /*
         * Step 2:
         * Submit message to the topic created in previous step.
         */
        TransactionResponse sendResponse = new TopicMessageSubmitTransaction()
            .setTopicId(createReceipt.topicId)
            .setMessage("Hello World")
            .execute(client);

        TransactionReceipt sendReceipt = sendResponse.getReceipt(client);
        System.out.println("topic sequence number = " + sendReceipt.topicSequenceNumber);

        /*
         * Clean up:
         * Delete created topic.
         */
        new TopicDeleteTransaction()
            .setTopicId(createReceipt.topicId)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
