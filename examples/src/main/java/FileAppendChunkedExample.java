/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
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
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.FileInfo;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class FileAppendChunkedExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private FileAppendChunkedExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        TransactionResponse transactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .setKeys(OPERATOR_KEY.getPublicKey())
            .setContents("Hello from Hedera.")
            // The default max fee of 1 HBAR is not enough to make a file ( starts around 1.1 HBAR )
            .setMaxTransactionFee(new Hbar(2)) // 2 HBAR
            .execute(client);

        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(receipt.fileId);

        System.out.println("fileId: " + newFileId);

        StringBuilder contents = new StringBuilder();

        for (int i = 0; i <= 4096 * 9; i++) {
            contents.append("1");
        }

        TransactionReceipt fileAppendReceipt = new FileAppendTransaction()
            .setNodeAccountIds(Collections.singletonList(transactionResponse.nodeId))
            .setFileId(newFileId)
            .setContents(contents.toString())
            .setMaxChunks(40)
            .setMaxTransactionFee(new Hbar(1000))
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        System.out.println(fileAppendReceipt.toString());

        FileInfo info = new FileInfoQuery()
            .setFileId(newFileId)
            .execute(client);

        System.out.println("File size according to `FileInfoQuery`: " + info.size);
    }
}
