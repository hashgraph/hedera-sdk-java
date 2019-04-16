package com.hedera.sdk.examples;

import com.hedera.sdk.account.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.account.AccountBalanceQuery;
import com.hedera.sdk.account.CryptoTransferTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Map;
import java.util.Objects;

@SuppressWarnings("Duplicates")
public final class GetAccountBalance {
    public static void main(String[] args) {
        var env = Dotenv.load();

        var operatorKey = Ed25519PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_SECRET")));
        AccountId operator;

        operator = AccountId.fromString(Objects.requireNonNull(env.get("OPERATOR")));

        var network = Objects.requireNonNull(env.get("NETWORK"));
        var node = AccountId.fromString(Objects.requireNonNull(env.get("NODE")));

        var client = new Client(Map.of(node, network));

        // Account balance query requires 100,000 tinybar
        var txPayment = new CryptoTransferTransaction(client).setNodeAccountId(node)
            .setTransactionId(new TransactionId(node))
            .addSender(operator, 100000)
            .addRecipient(node, 100000)
            .sign(operatorKey);

        var query = new AccountBalanceQuery(client).setAccount(operator)
            .setPayment(txPayment);

        try {
            var balance = query.execute();
            System.out.println(balance.toString());
        } catch (HederaException e) {
            System.out.println("Failed to get account balance: " + e);
        }
    }

}
