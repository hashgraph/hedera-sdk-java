package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.file.FileInfo;
import com.hedera.hashgraph.sdk.file.FileInfoQuery;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class DeleteFile {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private DeleteFile() { }

    public static void main(String[] args) throws HederaException {
        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        Client client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        byte[] fileContents = "Hedera hashgraph is great!".getBytes();

        FileCreateTransaction tx = new FileCreateTransaction(client).setExpirationTime(
            Instant.now()
                .plus(Duration.ofSeconds(2592000)))
            // Use the same key as the operator to "own" this file
            .addKey(OPERATOR_KEY.getPublicKey())
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
