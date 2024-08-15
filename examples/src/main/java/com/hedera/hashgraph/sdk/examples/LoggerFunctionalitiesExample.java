/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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

import java.util.Objects;

/**
 * How SDK Logger works.
 *
 * TODO: this example should be revisited/updated, as there is almost no logging with showcased logging levels.
 */
public class LoggerFunctionalitiesExample {

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

    public static void main(String[] args) throws Exception {
        System.out.println("Logger Functionalities Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1:
         * Instantiate debug- and info-level loggers.
         */
        var debugLogger = new Logger(LogLevel.DEBUG);
        var infoLogger = new Logger(LogLevel.INFO);

        /*
         * Step 2:
         * Attach debug logger to the SDK Client.
         */
        client.setLogger(debugLogger);

        /*
         * Step 3:
         * Generate ED25519 private and public keys.
         */
        var privateKey = PrivateKey.generateED25519();
        var publicKey = privateKey.getPublicKey();

        /*
         * Step 4:
         * "Create" account.
         */
        System.out.println("\"Creating\" new account...");
        var aliasAccountId = publicKey.toAccountId(0, 0);
        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 4:
         * Transfer 10 tinybars from operator's account to newly created account to init it on Hedera network.
         */
        System.out.println("Transferring Hbar to the the new account...");
        new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, Hbar.fromTinybars(-10))
            .addHbarTransfer(aliasAccountId, Hbar.fromTinybars(10))
            .setTransactionMemo("")
            .execute(client);

        /*
         * Step 5:
         * Create a topic with attached info logger.
         */
        System.out.println("Creating new topic...(with attached info logger).");
        var topicId1 = new TopicCreateTransaction()
            .setLogger(infoLogger)
            .setTopicMemo("topic memo")
            .setAdminKey(operatorPublicKey)
            .execute(client)
            .getReceipt(client)
            .topicId;

        /*
         * Step 6:
         * Set the level of the infoLogger from info to error.
         */
        infoLogger.setLevel(LogLevel.ERROR);

        /*
         * Step 7:
         * Create a topic with attached info logger.
         *
         * This should not display any logs because currently there are no warn logs predefined in the SDK.
         */
        System.out.println("Creating new topic...(with attached info logger).");
        var topicId2 = new TopicCreateTransaction()
            .setLogger(infoLogger)
            .setTopicMemo("topic memo")
            .setAdminKey(operatorPublicKey)
            .execute(client)
            .getReceipt(client)
            .topicId;

        /*
         * Step 8:
         * Silence the debugLogger - no logs should be shown.
         *
         * This can also be achieved by calling .setLevel(LogLevel.Silent).
         */
        debugLogger.setSilent(true);

        /*
         * Step 9:
         * Create a topic with attached debug logger.
         * This should not display any logs because logger was silenced.
         */
        System.out.println("Creating new topic...(with attached debug logger).");
        var topicId3 = new TopicCreateTransaction()
            .setLogger(debugLogger)
            .setTopicMemo("topic memo")
            .setAdminKey(operatorPublicKey)
            .execute(client)
            .getReceipt(client)
            .topicId;

        /*
         * Step 10:
         * Unsilence the debugLogger - applies back the old log level before silencing.
         */
        debugLogger.setSilent(false);

        /*
         * Step 11:
         * Create a topic with attached debug logger.
         *
         * Should produce logs.
         */
        System.out.println("Creating new topic...(with attached debug logger).");
        var topicId4 = new TopicCreateTransaction()
            .setLogger(debugLogger)
            .setTopicMemo("topicMemo")
            .setAdminKey(operatorPublicKey)
            .execute(client)
            .getReceipt(client)
            .topicId;

        /*
         * Clean up:
         * Delete created topics.
         */
        new TopicDeleteTransaction()
            .setTopicId(topicId1)
            .execute(client)
            .getReceipt(client);

        new TopicDeleteTransaction()
            .setTopicId(topicId2)
            .execute(client)
            .getReceipt(client);

        new TopicDeleteTransaction()
            .setTopicId(topicId3)
            .execute(client)
            .getReceipt(client);

        new TopicDeleteTransaction()
            .setTopicId(topicId4)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Logger Functionalities Example Complete!");
    }
}
