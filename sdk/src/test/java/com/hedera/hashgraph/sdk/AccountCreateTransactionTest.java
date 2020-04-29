package com.hedera.hashgraph.sdk;

import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static io.github.jsonSnapshot.SnapshotMatcher.*;

public class AccountCreateTransactionTest {
    private static final PrivateKey privateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    final Instant instant = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    Transaction setTransaction() {
        var client = Client.forTestnet();
        return new AccountCreateTransaction()
            .setNodeAccountId(AccountId.fromString("0.0.5005"))
            .setTransactionId(new TransactionId(AccountId.fromString("0.0.5006"), instant))
            .setKey(privateKey.getPublicKey())
            .setInitialBalance(Hbar.fromTinybar(450))
            .setProxyAccountId(AccountId.fromString("0.0.1001"))
            .setReceiverSignatureRequired(true)
            .setMaxTransactionFee(Hbar.fromTinybar(100_000))
            .build(client)
            .sign(privateKey);
    }

    @Test
    @DisplayName("object to be sent matches snapshot")
    void matchesSnap() {
        expect(setTransaction().makeRequest()).toMatchSnapshot();
    }
}
