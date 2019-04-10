package com.hedera.sdk.examples;

import com.hedera.sdk.*;
import com.hedera.sdk.account.AccountCreateTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("Duplicates")
public final class CreateAccount {
    public static void main(String[] args) throws InterruptedException {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));
        var newKey = Ed25519PrivateKey.generate();

        var network = Objects.requireNonNull(env.get("NETWORK"));
        var node = AccountId.fromString(Objects.requireNonNull(env.get("NODE")));

        var client = new Client(Map.of(node, network));

        var txId = new TransactionId(new AccountId(2));
        var tx = new AccountCreateTransaction(client).setTransactionId(txId)
            .setNodeAccount(new AccountId(3))
            .setKey(newKey.getPublicKey())
            // default (from transaction id): .setShardId(0)
            // default (from transaction id): .setRealmId(0)
            // default: .setAutoRenewPeriod(Duration.ofSeconds(2_592_000))
            // default: .setSendRecordThreshold(Long.MAX_VALUE)
            // default: .setReceiveRecordThreshold(Long.MAX_VALUE)
            // default: .setReceiverSignatureRequired(false)
            // default: .setInitialBalance(0)
            .sign(operatorKey);

        var res = tx.execute();

        System.out.println("transaction: " + res.toString());

        // Sleep for 4 seconds
        // TODO: We should make the query here retry internally if its "not ready" or "busy"
        Thread.sleep(4000);

        var query = new TransactionReceiptQuery(client).setTransaction(txId);

        var receipt = query.execute();
        var receiptStatus = receipt.getStatus();

        System.out.println("status: " + receiptStatus.toString());

        var newAccountId = receipt.getAccountId();
        assert newAccountId != null;
        System.out.println("new account num: " + newAccountId.getAccountNum());
    }
}
