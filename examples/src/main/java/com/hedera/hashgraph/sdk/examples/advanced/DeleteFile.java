package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.file.FileInfo;
import com.hedera.hashgraph.sdk.file.FileInfoQuery;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import java.time.Duration;
import java.time.Instant;

public final class DeleteFile {
    private DeleteFile() { }

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
            .setContents(fileContents);

        TransactionReceipt receipt = tx.executeForReceipt();
        FileId newFileId = receipt.getFileId();

        System.out.println("file: " + newFileId);

        // now delete the file
        TransactionReceipt txDeleteReceipt = new FileDeleteTransaction(client)
            .setFileId(newFileId)
            .executeForReceipt();

        if (txDeleteReceipt.getStatus() != ResponseCodeEnum.SUCCESS) {
            System.out.println("Error while deleting a file");
            return;
        }
        System.out.println("File deleted successfully.");

        FileInfo fileInfo = new FileInfoQuery(client)
            .setFileId(newFileId)
            .execute();

        // note the above fileInfo will fail with FILE_DELETED due to a known issue on Hedera

    }
}
