package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TokenCreateTransactionTest {
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
    void shouldSerializeFungible() {
        SnapshotMatcher.expect(spawnTestTransaction()
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldSerializeNft() {
        SnapshotMatcher.expect(spawnTestTransactionNft()
            .toString()
        ).toMatchSnapshot();
    }

    private TokenCreateTransaction spawnTestTransactionNft() {
        return new TokenCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setFeeScheduleKey(unusedPrivateKey)
            .setSupplyKey(unusedPrivateKey)
            .setMaxSupply(500)
            .setAdminKey(unusedPrivateKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.123"))
            .setAutoRenewPeriod(Duration.ofSeconds(100))
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setSupplyType(TokenSupplyType.FINITE)
            .setFreezeKey(unusedPrivateKey)
            .setWipeKey(unusedPrivateKey)
            .setTokenSymbol("F")
            .setKycKey(unusedPrivateKey)
            .setPauseKey(unusedPrivateKey)
            .setExpirationTime(validStart)
            .setTreasuryAccountId(AccountId.fromString("0.0.456"))
            .setTokenName("floof")
            .setTokenMemo("Floof says hi")
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesFungible() throws Exception {
        var tx = spawnTestTransaction();
        var tx2 = TokenCreateTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }

    private TokenCreateTransaction spawnTestTransaction() {
        return new TokenCreateTransaction()
            .setNodeAccountIds(Arrays.asList(AccountId.fromString("0.0.5005"), AccountId.fromString("0.0.5006")))
            .setTransactionId(TransactionId.withValidStart(AccountId.fromString("0.0.5006"), validStart))
            .setInitialSupply(30)
            .setFeeScheduleKey(unusedPrivateKey)
            .setSupplyKey(unusedPrivateKey)
            .setAdminKey(unusedPrivateKey)
            .setAutoRenewAccountId(AccountId.fromString("0.0.123"))
            .setAutoRenewPeriod(Duration.ofSeconds(100))
            .setDecimals(3)
            .setFreezeDefault(true)
            .setFreezeKey(unusedPrivateKey)
            .setWipeKey(unusedPrivateKey)
            .setTokenSymbol("F")
            .setKycKey(unusedPrivateKey)
            .setPauseKey(unusedPrivateKey)
            .setExpirationTime(validStart)
            .setTreasuryAccountId(AccountId.fromString("0.0.456"))
            .setTokenName("floof")
            .setTokenMemo("Floof says hi")
            .setCustomFees(Collections.singletonList(
                new CustomFixedFee()
                    .setFeeCollectorAccountId(AccountId.fromString("0.0.543"))
                    .setAmount(3)
                    .setDenominatingTokenId(TokenId.fromString("4.3.2"))
            ))
            .setMaxTransactionFee(new Hbar(1))
            .freeze()
            .sign(unusedPrivateKey);
    }

    @Test
    void shouldBytesNft() throws Exception {
        var tx = spawnTestTransactionNft();
        var tx2 = TokenCreateTransaction.fromBytes(tx.toBytes());
        assertEquals(tx.toString(), tx2.toString());
    }
}
