package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.AccountInfo;
import com.hedera.hashgraph.sdk.account.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class UpdateAccountPublicKey {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId NODE_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("NODE_ID")));
    private static final String NODE_ADDRESS = Objects.requireNonNull(Dotenv.load().get("NODE_ADDRESS"));
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private UpdateAccountPublicKey() { }

    public static void main(String[] args) throws HederaException {
        // To improve responsiveness, you should specify multiple nodes using the
        // `Client(<Map<AccountId, String>>)` constructor instead
        Client client = new Client(NODE_ID, NODE_ADDRESS);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        client.setMaxTransactionFee(800000000);

        // First, we create a new account so we don't affect our account

        Ed25519PrivateKey originalKey = Ed25519PrivateKey.generate();
        Transaction acctTransaction = new AccountCreateTransaction(client)
            .setMaxTransactionFee(1_000_000_000)
            .setKey(originalKey.getPublicKey())
            .setInitialBalance(100_000_000)
            .build();

        System.out.println("transaction ID: " + acctTransaction.execute());
        AccountId accountId = acctTransaction.queryReceipt().getAccountId();
        System.out.println("account = " + accountId);
        // Next, we update the key

        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();

        System.out.println(" :: update public key of account " + accountId);
        System.out.println("set key = " + newKey.getPublicKey());

        Transaction transaction = new AccountUpdateTransaction(client)
            .setAccountForUpdate(accountId)
            .setKey(newKey.getPublicKey())
            // Sign with the previous key and the new key
            .sign(originalKey)
            .sign(newKey);

        System.out.println("transaction ID: " + transaction.id);

        transaction.execute();

        // Now we fetch the account information to check if the key was changed

        System.out.println(" :: getAccount and check our current key");

        AccountInfo info = client.getAccount(accountId);

        System.out.println("key = " + info.key);
    }
}
