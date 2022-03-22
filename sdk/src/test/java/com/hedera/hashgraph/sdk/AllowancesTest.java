package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.NftAllowance;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AllowancesTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    TokenAllowance spawnTokenAllowance() {
        return new TokenAllowance(
            TokenId.fromString("1.2.3"),
            AccountId.fromString("4.5.6"),
            AccountId.fromString("5.5.5"),
            777
        );
    }

    TokenNftAllowance spawnNftAllowance() {
        List<Long> serials = new ArrayList<>();
        serials.add(123L);
        serials.add(456L);
        return new TokenNftAllowance(
            TokenId.fromString("1.1.1"),
            AccountId.fromString("2.2.2"),
            AccountId.fromString("3.3.3"),
            serials,
            null
        );
    }

    TokenNftAllowance spawnAllNftAllowance() {
        return new TokenNftAllowance(
            TokenId.fromString("1.1.1"),
            AccountId.fromString("2.2.2"),
            AccountId.fromString("3.3.3"),
            Collections.emptyList(),
            true
        );
    }

    HbarAllowance spawnHbarAllowance() {
        return new HbarAllowance(AccountId.fromString("1.1.1"), AccountId.fromString("2.2.2"), new Hbar(3));
    }

    @Test
    void shouldSerialize() {
        SnapshotMatcher.expect(
            spawnHbarAllowance().toString(),
            spawnTokenAllowance().toString(),
            spawnNftAllowance().toString(),
            spawnAllNftAllowance().toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldBytes() throws InvalidProtocolBufferException {
        var hbar1 = spawnHbarAllowance();
        var token1 = spawnTokenAllowance();
        var nft1 = spawnNftAllowance();
        var allNft1 = spawnAllNftAllowance();
        var hbar2 = HbarAllowance.fromBytes(hbar1.toBytes());
        var token2 = TokenAllowance.fromBytes(token1.toBytes());
        var nft2 = TokenNftAllowance.fromBytes(nft1.toBytes());
        var allNft2 = TokenNftAllowance.fromBytes(allNft1.toBytes());
        assertEquals(hbar1.toString(), hbar2.toString());
        assertEquals(token1.toString(), token2.toString());
        assertEquals(nft1.toString(), nft2.toString());
        assertEquals(allNft1.toString(), allNft2.toString());
    }
}
