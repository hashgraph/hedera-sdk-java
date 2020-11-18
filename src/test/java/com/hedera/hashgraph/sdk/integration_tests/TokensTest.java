package com.hedera.hashgraph.sdk.integration_tests;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.token.*;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Objects;

class TokensTest {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final Ed25519PrivateKey OPERATOR_KEY = Ed25519PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");

    @Test
    void issue376() throws IOException, HederaStatusException {
        Client client;

        if (HEDERA_NETWORK != null && HEDERA_NETWORK.equals("previewnet")) {
            client = Client.forPreviewnet();
        } else {
            try {
                client = Client.fromFile(CONFIG_FILE != null ? CONFIG_FILE : "");
            } catch (FileNotFoundException e) {
                client = Client.forTestnet();
            }
        }

        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

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


