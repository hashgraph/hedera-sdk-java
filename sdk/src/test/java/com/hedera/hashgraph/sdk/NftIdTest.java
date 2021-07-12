package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;

import com.google.protobuf.InvalidProtocolBufferException;

import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NftIdTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromString() {
        SnapshotMatcher.expect(NftId.fromString("0.0.5005@1234").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnMainnet() {
        SnapshotMatcher.expect(NftId.fromString("0.0.123-vfmkw@7584").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnTestnet() {
        SnapshotMatcher.expect(NftId.fromString("0.0.123-rmkyk@584903").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnPreviewnet() {
        SnapshotMatcher.expect(NftId.fromString("0.0.123-ntjly@487302").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnUndefinedNetwork() {
        assertThrows(IllegalArgumentException.class, () -> NftId.fromString("0.0.123-ghgna@3894"));
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(Hex.toHexString(new TokenId(5005).nft(4920).toBytes())).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(NftId.fromBytes(new TokenId(5005).nft(574489).toBytes()).toString()).toMatchSnapshot();
    }
}
