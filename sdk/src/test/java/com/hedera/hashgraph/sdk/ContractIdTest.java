package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractIdTest {
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
        SnapshotMatcher.expect(ContractId.fromString("0.0.5005").toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddress() {
        SnapshotMatcher.expect(ContractId.fromSolidityAddress("000000000000000000000000000000000000138D").toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddressWith0x() {
        SnapshotMatcher.expect(ContractId.fromSolidityAddress("0x000000000000000000000000000000000000138D").toString()).toMatchSnapshot();
    }

    @Test
    void fromEvmAddress() {
        SnapshotMatcher.expect(ContractId.fromEvmAddress(1, 2, "98329e006610472e6B372C080833f6D79ED833cf").toString()).toMatchSnapshot();
    }

    @Test
    void fromEvmAddressWith0x() {
        SnapshotMatcher.expect(ContractId.fromEvmAddress(1, 2, "0x98329e006610472e6B372C080833f6D79ED833cf").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithEvmAddress() {
        SnapshotMatcher.expect(ContractId.fromString("1.2.98329e006610472e6B372C080833f6D79ED833cf").toString()).toMatchSnapshot();
    }

    @Test
    void toFromBytes() throws InvalidProtocolBufferException {
        ContractId a = ContractId.fromString("1.2.3");
        assertEquals(a, ContractId.fromBytes(a.toBytes()));
        ContractId b = ContractId.fromEvmAddress(1, 2, "0x98329e006610472e6B372C080833f6D79ED833cf");
        assertEquals(b, ContractId.fromBytes(b.toBytes()));
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(Hex.toHexString(new ContractId(5005).toBytes())).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractId.fromBytes(new ContractId(5005).toBytes()).toString()).toMatchSnapshot();
    }

    @Test
    void toSolidityAddress() {
        SnapshotMatcher.expect(new ContractId(5005).toSolidityAddress()).toMatchSnapshot();
    }

    @Test
    void toSolidityAddress2() {
        SnapshotMatcher.expect(ContractId.fromEvmAddress(1, 2, "0x98329e006610472e6B372C080833f6D79ED833cf").toSolidityAddress()).toMatchSnapshot();
    }
}
