package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

public class AccountUpdateTransactionTest {
    private static final PrivateKey unusedPrivateKey = PrivateKey.fromString(
        "302e020100300506032b657004220420db484b828e64b2d8f12ce3c0a0e93a0b8cce7af1bb8f39c97732394482538e10");

    final Instant validStart = Instant.ofEpochSecond(1554158542);

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(new AccountUpdateTransaction()
            .setKey(unusedPrivateKey)
            .setNodeAccountId(AccountId.fromString("0.0.5005"))
            .setTransactionId(new TransactionId(AccountId.fromString("0.0.5006"), validStart))
            .setAccountId(AccountId.fromString("0.0.2002"))
            .setProxyAccountId(AccountId.fromString("0.0.1001"))
            .setSendRecordThreshold(Hbar.fromTinybars(5))
            .setReceiveRecordThreshold(Hbar.fromTinybars(6))
            .setAutoRenewPeriod(Duration.ofHours(10))
            .setExpirationTime(Instant.ofEpochSecond(1554158543))
            .setReceiverSignatureRequired(false)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .build(Client.forTestnet())
            .sign(unusedPrivateKey)
            .toString()
        ).toMatchSnapshot();
    }
}
