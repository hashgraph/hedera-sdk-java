package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;

import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;

import com.hedera.hashgraph.sdk.token.*;
import io.github.cdimascio.dotenv.Dotenv;

public final class TokenTransfers {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String CONFIG_FILE = Objects.requireNonNull(Dotenv.load().get("CONFIG_FILE"));

    private TokenTransfers() { }

    public static void main(String[] args) throws HederaStatusException, FileNotFoundException {
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        Client client = Client.fromFile(CONFIG_FILE).setOperator(OPERATOR_ID, OPERATOR_KEY);

        TransactionId txId = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newKey.publicKey)
            .setInitialBalance(Hbar.fromTinybar(1000))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = txId.getReceipt(client);

        AccountId newAccountId = receipt.getAccountId();

        System.out.println("account = " + newAccountId);

        txId = new TokenCreateTransaction()
            .setName("ffff")
            .setSymbol("F")
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setTreasury(OPERATOR_ID)
            .setAdminKey(OPERATOR_KEY.publicKey)
            .setFreezeKey(OPERATOR_KEY.publicKey)
            .setWipeKey(OPERATOR_KEY.publicKey)
            .setKycKey(OPERATOR_KEY.publicKey)
            .setSupplyKey(OPERATOR_KEY.publicKey)
            .setFreezeDefault(false)
            .setExpirationTime(Instant.now().plus(Duration.ofDays(90)))
            .execute(client);

        TokenId tokenId = txId.getReceipt(client).getTokenId();
        System.out.println("New token created: " + tokenId);

        new TokenAssociateTransaction()
            .setAccountId(newAccountId)
            .addTokenId(tokenId)
            .build(client)
            .sign(OPERATOR_KEY)
            .sign(newKey)
            .execute(client)
            .getReceipt(client);

        new TokenGrantKycTransaction()
            .setAccountId(newAccountId)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        new TokenTransferTransaction()
            .addSender(tokenId, OPERATOR_ID, 10)
            .addRecipient(tokenId, newAccountId, 10)
            .execute(client)
            .getReceipt(client);
    }
}
