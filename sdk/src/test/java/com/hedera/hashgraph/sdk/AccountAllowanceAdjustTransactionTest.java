package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountAllowanceAdjustTransactionTest {
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

    AccountAllowanceAdjustTransaction spawnTestTransaction() {
        var ownerId = AccountId.fromString("5.6.7");
        return new AccountAllowanceAdjustTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .addHbarAllowance(AccountId.fromString("1.1.1"), new Hbar(3))
            .addTokenAllowance(TokenId.fromString("2.2.2"), AccountId.fromString("3.3.3"), 6)
            .addTokenNftAllowance(TokenId.fromString("4.4.4").nft(123), AccountId.fromString("5.5.5"))
            .addTokenNftAllowance(TokenId.fromString("4.4.4").nft(456), AccountId.fromString("5.5.5"))
            .addTokenNftAllowance(TokenId.fromString("8.8.8").nft(456), AccountId.fromString("5.5.5"))
            .addTokenNftAllowance(TokenId.fromString("4.4.4").nft(789), AccountId.fromString("9.9.9"))
            .addAllTokenNftAllowance(TokenId.fromString("6.6.6"), AccountId.fromString("7.7.7"))
            .addHbarAllowanceWithOwner(AccountId.fromString("1.1.1"), new Hbar(3), ownerId)
            .addTokenAllowanceWithOwner(TokenId.fromString("2.2.2"), AccountId.fromString("3.3.3"), 6, ownerId)
            .addTokenNftAllowanceWithOwner(TokenId.fromString("4.4.4").nft(123), AccountId.fromString("5.5.5"), ownerId)
            .addTokenNftAllowanceWithOwner(TokenId.fromString("4.4.4").nft(456), AccountId.fromString("5.5.5"), ownerId)
            .addTokenNftAllowanceWithOwner(TokenId.fromString("8.8.8").nft(456), AccountId.fromString("5.5.5"), ownerId)
            .addTokenNftAllowanceWithOwner(TokenId.fromString("4.4.4").nft(789), AccountId.fromString("9.9.9"), ownerId)
            .addAllTokenNftAllowanceWithOwner(TokenId.fromString("6.6.6"), AccountId.fromString("7.7.7"), ownerId)
            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(spawnTestTransaction().toString()).toMatchSnapshot();
    }

    @Test
    void shouldBytes() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = AccountAllowanceAdjustTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
