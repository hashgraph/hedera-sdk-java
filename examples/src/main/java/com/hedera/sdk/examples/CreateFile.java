package com.hedera.sdk.examples;

import com.hedera.sdk.*;
import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.file.FileCreateTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("Duplicates")
public final class CreateFile {
    public static void main(String[] args) throws InterruptedException {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));

        var network = Objects.requireNonNull(env.get("NETWORK"));
        var node = AccountId.fromString(Objects.requireNonNull(env.get("NODE")));

        var client = new Client(Map.of(node, network));

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        var fileContents = "Hedera hashgraph is great!".getBytes();

        var txId = new TransactionId(new AccountId(2));

        var tx = new FileCreateTransaction(client).setTransactionId(txId)
            .setNodeAccountId(new AccountId(3))
            .setExpirationTime(
                Instant.now()
                    .plus(Duration.ofSeconds(2592000))
            )
            .addKey(operatorKey.getPublicKey())
            .setContents(fileContents)
            .setMemo("[hedera-sdk-java][example] CreateFile")
            // The first signature represents the transaction payer
            .sign(operatorKey)
            // The second signature represents the owner of the file
            .sign(operatorKey);

        TransactionReceipt receipt;
        try {
            receipt = tx.executeForReceipt();
        } catch (HederaException e) {
            System.out.println("Failed to create file: " + e);
            return;
        }

        var receiptStatus = receipt.getStatus();

        System.out.println("status: " + receiptStatus.toString());

        var newFileId = receipt.getFileId();
        assert newFileId != null;

        System.out.println("new file num: " + newFileId.getFileNum());
    }
}
