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

import java.util.Collections;
import java.util.Objects;

/**
 * How to append to already created file.
 */
class FileAppendChunkedExample {

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
        System.out.println("Big File Append Example Start!");

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
         * Submit the file create transaction.
         */
        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        String fileContents = "Hedera hashgraph is great!";

        System.out.println("Creating new file...");
        TransactionResponse transactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file.
            .setKeys(operatorPublicKey)
            .setContents(fileContents)
            // The default max fee of 1 Hbar is not enough to create a file (starts around ~1.1 Hbar).
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);

        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(receipt.fileId);

        System.out.println("Created new file with ID: " + newFileId);

        /*
         * Step 2:
         * Query file info to check its size after creation.
         */
        FileInfo fileInfoAfterCreate = new FileInfoQuery()
            .setFileId(newFileId)
            .execute(client);
        System.out.println("Created file size after create (according to `FileInfoQuery`): " + fileInfoAfterCreate.size + " bytes.");

        /*
         * Step 3:
         * Create new file contents that will be appended to a file.
         */
        StringBuilder contents = new StringBuilder();
        for (int i = 0; i <= 4096 * 9; i++) {
            contents.append("1");
        }

        /*
         * Step 4:
         * Append new file contents to a file.
         */
        System.out.println("Appending new contents to the created file...");
        new FileAppendTransaction()
            .setNodeAccountIds(Collections.singletonList(transactionResponse.nodeId))
            .setFileId(newFileId)
            .setContents(contents.toString())
            .setMaxChunks(40)
            .setMaxTransactionFee(new Hbar(1_000))
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 5:
         * Query file info to check its size after append.
         */
        FileInfo fileInfoAfterAppend = new FileInfoQuery()
            .setFileId(newFileId)
            .execute(client);

        if (fileInfoAfterCreate.size < fileInfoAfterAppend.size) {
            System.out.println("File size after append (according to `FileInfoQuery`): " + fileInfoAfterAppend.size + " bytes.");
        } else {
            throw new Exception("File append was unsuccessful! (Fail)");
        }

        /*
         * Clean up:
         * Delete created file.
         */
        new FileDeleteTransaction()
            .setFileId(newFileId)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Big File Append Example Complete!");
    }
}
