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

import java.util.Collections;
import java.util.Objects;

/**
 * How to append to already created file.
 */
class FileAppendChunkedExample {

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
         * Submit the file create transaction.
         */
        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        String fileContents = "Hedera hashgraph is great!";

        TransactionResponse transactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file.
            .setKeys(operatorPublicKey)
            .setContents(fileContents)
            // The default max fee of 1 Hbar is not enough to create a file (starts around ~1.1 Hbar).
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);

        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(receipt.fileId);

        System.out.println("fileId: " + newFileId);

        /*
         * Step 2:
         * Query file info to check its size after creation.
         */
        FileInfo fileInfoAfterCreate = new FileInfoQuery()
            .setFileId(newFileId)
            .execute(client);

        System.out.println("File size according to `FileInfoQuery` (after create): " + fileInfoAfterCreate.size);

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
        TransactionReceipt fileAppendReceipt = new FileAppendTransaction()
            .setNodeAccountIds(Collections.singletonList(transactionResponse.nodeId))
            .setFileId(newFileId)
            .setContents(contents.toString())
            .setMaxChunks(40)
            .setMaxTransactionFee(new Hbar(1_000))
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        System.out.println(fileAppendReceipt.toString());

        /*
         * Step 5:
         * Query file info to check its size after append.
         */
        FileInfo fileInfoAfterAppend = new FileInfoQuery()
            .setFileId(newFileId)
            .execute(client);

        if (fileInfoAfterCreate.size < fileInfoAfterAppend.size) {
            System.out.println("File size according to `FileInfoQuery` (after append): " + fileInfoAfterAppend.size);
        } else {
            throw new Exception("File append was unsuccessful");
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

        System.out.println("Example complete!");
    }
}
