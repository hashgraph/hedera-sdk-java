package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hederahashgraph.api.proto.java.FileGetContentsResponse;

import java.time.Duration;
import java.time.Instant;

public final class GetFileContents {
    private GetFileContents() { }

    public static void main(String[] args) throws HederaException {

        // Grab the private key from the .env file
        Ed25519PrivateKey operatorKey = ExampleHelper.getOperatorKey();

        // Build the Hedera client using ExampleHelper class
        Client client = ExampleHelper.createHederaClient();

        // Content to be stored in the file
        byte[] fileContents = ("Hedera is great!").getBytes();

        // Create the new file and set its properties
        TransactionReceipt newFile = new FileCreateTransaction(client)
            .addKey(operatorKey.getPublicKey()) // The public key of the owner of the file
            .setContents(fileContents) // Contents of the file
            .setExpirationTime(Instant.now().plus(Duration.ofSeconds(2592000))) // Set file expiration time in seconds
            .executeForReceipt(); // Submits transaction to the network and returns receipt which contains file ID

        //Print the file ID to console
        System.out.println("The new file ID is " + newFile.getFileId().toString());

        // Get file contents
        FileGetContentsResponse contents = new FileContentsQuery(client)
            .setFileId(newFile.getFileId())
            .execute();

        // Prints query results to console
        System.out.println("File content query results: " + contents.getFileContents().getContents().toStringUtf8());
    }

}
