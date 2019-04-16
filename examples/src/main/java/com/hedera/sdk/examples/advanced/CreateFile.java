package com.hedera.sdk.examples.advanced;

import com.hedera.sdk.*;
import com.hedera.sdk.examples.ExampleHelper;
import com.hedera.sdk.file.FileCreateTransaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public final class CreateFile {
    public static void main(String[] args) throws HederaException {
        var operatorKey = ExampleHelper.getOperatorKey();
        var client = ExampleHelper.createHederaClient();

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        var fileContents = "Hedera hashgraph is great!".getBytes();

        var tx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000))
        )
            // Use the same key as the operator to "own" this file
            .addKey(operatorKey.getPublicKey())
            .setContents(fileContents);

        var receipt = tx.executeForReceipt();
        var newFileId = Objects.requireNonNull(receipt.getFileId());

        System.out.println("file: " + newFileId);
    }
}
