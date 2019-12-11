package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.proto.FileGetContentsResponse;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class GetFileContents {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private GetFileContents() { }

    public static void main(String[] args) throws HederaException {
        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        Client client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Content to be stored in the file
        byte[] fileContents = ("Hedera is great!").getBytes();

        // Create the new file and set its properties
        Transaction newFileTx = new FileCreateTransaction()
            .addKey(OPERATOR_KEY.getPublicKey()) // The public key of the owner of the file
            .setContents(fileContents) // Contents of the file
            .setExpirationTime(Instant.now().plus(Duration.ofSeconds(2592000))) // Set file expiration time in seconds
            .build(client); // Submits transaction to the network and returns receipt which contains file ID

        newFileTx.execute(client);

        FileId newFileId = newFileTx.getReceipt(client).getFileId();

        //Print the file ID to console
        System.out.println("The new file ID is " + newFileId.toString());

        // Get file contents
        FileGetContentsResponse contents = new FileContentsQuery()
            .setFileId(newFileId)
            .execute(client);

        // Prints query results to console
        System.out.println("File content query results: " + contents.getFileContents().getContents().toStringUtf8());
    }

}
