package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;

import java.time.Duration;
import java.time.Instant;

public class GetFileContents {
    public static void main(String[] args) throws HederaException{

        // Grabs the private key from the .env file
        var operatorKey = ExampleHelper.getOperatorKey();

        // Build the Hedera client using the ExampleHelper class
        var client = ExampleHelper.createHederaClient();

        // Content to be stored in the file 
        var fileContents = ("Hedera is great!").getBytes();

        // Create the new file and set its properties
        var newFile = new FileCreateTransaction(client)
            .addKey(operatorKey.getPublicKey()) // The public key of the owner of the file
            .setContents(fileContents) // Contents of the file
            .setExpirationTime(Instant.now().plus(Duration.ofSeconds(2592000))) // Set file expiration time in seconds
            .executeForReceipt(); // Submits transaction to the network and returns receipt which contains the file ID

        //Prints the file ID to console
        System.out.println("The new file ID is " +  newFile.getFileId().toString());


        // Get file contents
        var contents = new FileContentsQuery(client)
            .setFileId(newFile.getFileId())
            .execute();

        // Prints query results to console 
        System.out.println("File content query results: " + contents.getFileContents().getContents().toStringUtf8());
    }
}
