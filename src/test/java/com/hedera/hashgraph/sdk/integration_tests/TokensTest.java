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
    private final TestEnv testEnv = new TestEnv();

    {
        testEnv.client.setMaxTransactionFee(new Hbar(100));
    }

    @Test
    void issue376() throws IOException, HederaStatusException {
        // Generate a Ed25519 private, public key pair
        Ed25519PrivateKey newKey = Ed25519PrivateKey.generate();
        Ed25519PublicKey newPublicKey = newKey.publicKey;

        TransactionId txId = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newKey.publicKey)
            .setInitialBalance(Hbar.fromTinybar(1000))
            .execute(testEnv.client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = txId.getReceipt(testEnv.client);

        AccountId newAccountId1 = receipt.getAccountId();

        txId = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newKey.publicKey)
            .setInitialBalance(Hbar.fromTinybar(1000))
            .execute(testEnv.client);

        // This will wait for the receipt to become available
        receipt = txId.getReceipt(testEnv.client);

        AccountId newAccountId2 = receipt.getAccountId();

        txId = new TokenCreateTransaction()
            .setName("ffff")
            .setSymbol("F")
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setTreasury(newAccountId1)
            .setAdminKey(newPublicKey)
            .setFreezeKey(newPublicKey)
            .setWipeKey(newPublicKey)
            .setKycKey(newPublicKey)
            .setSupplyKey(newPublicKey)
            .setFreezeDefault(false)
            .setExpirationTime(Instant.now().plus(Duration.ofDays(90)))
            .build(testEnv.client)
            .sign(newKey)
            .execute(testEnv.client);

        TokenId tokenId = txId.getReceipt(testEnv.client).getTokenId();

        new TokenAssociateTransaction()
            .setAccountId(newAccountId2)
            .addTokenId(tokenId)
            .build(testEnv.client)
            .sign(newKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenGrantKycTransaction()
            .setAccountId(newAccountId2)
            .setTokenId(tokenId)
            .build(testEnv.client)
            .sign(newKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TokenTransferTransaction()
            .addSender(tokenId, newAccountId1, 10)
            .addRecipient(tokenId, newAccountId2, 10)
            .build(testEnv.client)
            .sign(newKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);
    }
}


