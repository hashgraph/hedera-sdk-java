package com.hedera.sdk.examples;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.account.AccountBalanceQuery;
import com.hedera.sdk.account.CryptoTransferTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

public final class GetAccount {
    public static void main (String[] args) {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));
        AccountId operator;

        try {
            operator = AccountId.fromString(Objects.requireNonNull(env.get("OPERATOR")));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }

        var newKey = Ed25519PrivateKey.generate();
        var account = new AccountId(2);

        var client = new Client(env.get("NETWORK"));

        // Account balance query requires 100,000 tinybar
        var txPayment = new CryptoTransferTransaction()
            .setTransactionId(new TransactionId(operator))
            .addSender(operator, 100000)
            .addRecipient(account, 100000)
            .sign(operatorKey)
            .sign(newKey);

        var txId = new TransactionId(new AccountId(2));
        var tx = new AccountBalanceQuery()
            .setAccount(new AccountId(3))
            .setPayment(txPayment);

        var balance = tx.execute(client);

        System.out.println(balance.toString());

    }

}
