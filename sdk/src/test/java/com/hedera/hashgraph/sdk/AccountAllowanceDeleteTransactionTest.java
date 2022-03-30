package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountAllowanceDeleteTransactionTest {
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

    AccountAllowanceDeleteTransaction spawnTestTransaction() {
        var ownerId = AccountId.fromString("5.6.7");
        return new AccountAllowanceDeleteTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .deleteAllHbarAllowances(ownerId)
            .deleteAllTokenAllowances(TokenId.fromString("2.2.2"), ownerId)
            .deleteAllTokenNftAllowances(TokenId.fromString("4.4.4").nft(123), ownerId)
            .deleteAllTokenNftAllowances(TokenId.fromString("4.4.4").nft(456), ownerId)
            .deleteAllTokenNftAllowances(TokenId.fromString("8.8.8").nft(456), ownerId)
            .deleteAllTokenNftAllowances(TokenId.fromString("4.4.4").nft(789), ownerId)
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
        var tx2 = AccountAllowanceApproveTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
