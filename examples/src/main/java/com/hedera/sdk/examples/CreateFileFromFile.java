package com.hedera.sdk.examples;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionGetReceiptQuery;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.file.FileCreateTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@SuppressWarnings("Duplicates")
public final class CreateFileFromFile {
    public static void main(String[] args) throws InterruptedException, IOException {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(env.get("OPERATOR_SECRET"));
        var operator = new AccountId(2);

        var client = new Client(env.get("NETWORK"));

        byte[] fileContents;

        try(var inFile = new FileInputStream("hedera.txt")) {
            fileContents = inFile.readAllBytes();
        }

        var txId = new TransactionId(new AccountId(2));

        var tx = new FileCreateTransaction()
            .setTransactionId(txId)
            .setNodeAccountId(new AccountId(3))
            .setExpirationTime(Instant.now().plus(Duration.ofSeconds(2592000)))
            .addKey(operatorKey.getPublicKey())
            .setContents(fileContents)
            .setMemo("[hedera-sdk-java][example] CreateFile")
            .sign(operatorKey);

        var res = tx.execute(client);

        System.out.println("transaction: " + res.toString());

        // Sleep for 4 seconds
        Thread.sleep(4000);

        var query = new TransactionGetReceiptQuery()
            .setTransaction(txId);

        var receipt = query.execute(client);
        var receiptStatus = receipt.getReceipt().getStatus();

        System.out.println("status: " + receiptStatus.toString());

        var newFileId = receipt.getReceipt().getFileID();

        System.out.println("new file num: " + newFileId.getFileNum());
    }
}

