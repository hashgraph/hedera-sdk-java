package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import java.time.Duration;
import java.time.Instant;

public final class CreateFile {
    private CreateFile() { }

    public static void main(String[] args) throws HederaException {
        Ed25519PrivateKey operatorKey = ExampleHelper.getOperatorKey();
        Client client = ExampleHelper.createHederaClient();

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        byte[] fileContents = "Hedera hashgraph is great!".getBytes();

        FileCreateTransaction tx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000)))
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setTransactionFee(100_000_000)
            .setContents(fileContents);

        TransactionReceipt receipt = tx.executeForReceipt();
        FileId newFileId = receipt.getFileId();

        System.out.println("file: " + newFileId);
    }
}
