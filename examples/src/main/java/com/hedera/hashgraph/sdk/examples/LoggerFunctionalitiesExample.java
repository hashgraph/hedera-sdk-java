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

public class LoggerFunctionalitiesExample {

    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws Exception {
        var debugLogger = new Logger(LogLevel.DEBUG);
        var infoLogger = new Logger(LogLevel.INFO);

        Client client = ClientHelper.forName(HEDERA_NETWORK);

        client.setLogger(debugLogger);
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var privateKey = PrivateKey.generateED25519();
        var publicKey = privateKey.getPublicKey();
        var aliasAccountId = publicKey.toAccountId(0, 0);

        new TransferTransaction()
            .addHbarTransfer(client.getOperatorAccountId(), Hbar.fromTinybars(-10))
            .addHbarTransfer(aliasAccountId, Hbar.fromTinybars(10))
            .setTransactionMemo("")
            .execute(client);

        var topicId1 = new TopicCreateTransaction()
            .setLogger(infoLogger)
            .setTopicMemo("topic memo")
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .execute(client)
            .getReceipt(client)
            .topicId;

        // Set the level of the `infoLogger` from `info` to `error`
        infoLogger.setLevel(LogLevel.ERROR);

        // This should not display any logs because currently there are no `warn` logs predefined in the SDK
        var topicId2 = new TopicCreateTransaction()
            .setLogger(infoLogger)
            .setTopicMemo("topic memo")
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .execute(client)
            .getReceipt(client)
            .topicId;

        // Silence the `debugLogger` - no logs should be shown
        // This can also be achieved by calling `.setLevel(LogLevel.Silent)`
        debugLogger.setSilent(true);

        var topicId3 = new TopicCreateTransaction()
            .setLogger(debugLogger)
            .setTopicMemo("topic memo")
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .execute(client)
            .getReceipt(client)
            .topicId;

        // Unsilence the `debugLogger` - applies back the old log level before silencing
        debugLogger.setSilent(false);

        var topicId4 = new TopicCreateTransaction()
            .setLogger(debugLogger)
            .setTopicMemo("topicMemo")
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .execute(client)
            .getReceipt(client)
            .topicId;

        // Clean up

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
    }
}
