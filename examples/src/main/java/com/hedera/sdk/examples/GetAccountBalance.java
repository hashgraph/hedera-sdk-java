package com.hedera.sdk.examples;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.Target;
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

        var node = new AccountId(3);

        var client = new Client(Target.TESTNET_139);

        // Account balance query requires 100,000 tinybar
        var txPayment = new CryptoTransferTransaction().setNodeAccount(node)
            .setTransactionId(new TransactionId(node))
            .addSender(operator, 100000)
            .addRecipient(node, 100000)
            .sign(operatorKey);

        var query = new AccountBalanceQuery().setAccount(operator)
            .setPayment(txPayment);

        var balance = query.execute(client);

        System.out.println(balance.toString());

    }

}
