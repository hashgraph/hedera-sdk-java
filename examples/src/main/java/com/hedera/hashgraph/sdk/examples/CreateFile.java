package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.file.FileCreateTransaction;
import com.hedera.hashgraph.sdk.file.FileId;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class CreateFile {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private CreateFile() { }

    public static void main(String[] args) throws HederaStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        // `Client.forPreviewnet()` is provided for connecting to Hedera previewNet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        byte[] fileContents = "Hedera hashgraph is great!".getBytes();

        TransactionId txId = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .addKey(OPERATOR_KEY.publicKey)
            .setContents(fileContents)
            // The default max fee of 1 HBAR is not enough to make a file ( starts around 1.1 HBAR )
            .setMaxTransactionFee(200_000_000) // 2 HBAR
            .execute(client);

        TransactionReceipt receipt = txId.getReceipt(client);
        FileId newFileId = receipt.getFileId();

        System.out.println("file: " + newFileId);
    }
}
