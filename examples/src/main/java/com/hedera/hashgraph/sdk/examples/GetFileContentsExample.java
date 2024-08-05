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

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class GetFileContentsExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private GetFileContentsExample() {
    }

    public static void main(String[] args)
        throws ReceiptStatusException, TimeoutException, PrecheckStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Content to be stored in the file
        byte[] fileContents = "Hedera is great!".getBytes(StandardCharsets.UTF_8);

        // Create the new file and set its properties
        TransactionResponse newFileTransactionResponse = new FileCreateTransaction()
            .setKeys(OPERATOR_KEY) // The public key of the owner of the file
            .setContents(fileContents) // Contents of the file
            .setMaxTransactionFee(new Hbar(2))
            .execute(client);

        FileId newFileId = Objects.requireNonNull(newFileTransactionResponse.getReceipt(client).fileId);

        //Print the file ID to console
        System.out.println("The new file ID is " + newFileId.toString());

        // Get file contents
        ByteString contents = new FileContentsQuery()
            .setFileId(newFileId)
            .execute(client);

        // Prints query results to console
        System.out.println("File content query results: " + contents.toStringUtf8());

        client.close();
    }
}
