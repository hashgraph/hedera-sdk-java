package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;

import com.google.protobuf.InvalidProtocolBufferException;

import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

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
        SnapshotMatcher.expect(ContractId.fromString("0.0.5005").toProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddress() {
        SnapshotMatcher.expect(ContractId.fromSolidityAddress("000000000000000000000000000000000000138D").toProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddressWith0x() {
        SnapshotMatcher.expect(ContractId.fromSolidityAddress("0x000000000000000000000000000000000000138D").toProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(new ContractId(5005).toBytes()).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractId.fromBytes(new ContractId(5005).toBytes())).toMatchSnapshot();
    }

    @Test
    void toSolidityAddress() {
        SnapshotMatcher.expect(new ContractId(5005).toSolidityAddress()).toMatchSnapshot();
    }
}
