package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.AccountInfo;
import com.hedera.hashgraph.sdk.account.AccountInfoQuery;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import java.util.Objects;

import io.github.cdimascio.dotenv.Dotenv;

public final class DeleteAccount {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private DeleteAccount() { }

    public static void main(String[] args) throws HederaStatusException {
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        TransactionId txId = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newKey.publicKey)
            .setInitialBalance(Hbar.of(2))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = txId.getReceipt(client);

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);

        new AccountDeleteTransaction()
            // note the transaction ID has to use the ID of the account being deleted
            .setTransactionId(new TransactionId(newAccountId))
            .setDeleteAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .build(client)
            .sign(newKey)
            .execute(client)
            .getReceipt(client);

        final AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .setQueryPayment(25)
            .execute(client);

        // note the above accountInfo will fail with ACCOUNT_DELETED due to a known issue on Hedera

        System.out.println("account info: " + accountInfo);
    }
}
