package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeoutException;

class AccountIdTest {

    static Client mainnetClient = null;
    static Client testnetClient = null;
    static Client previewnetClient = null;

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
        mainnetClient = Client.forMainnet();
        testnetClient = Client.forTestnet();
        previewnetClient = Client.forPreviewnet();
    }

    @AfterClass
    public static void afterAll() throws TimeoutException {
        mainnetClient.close();
        testnetClient.close();
        previewnetClient.close();
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromString() {
        SnapshotMatcher.expect(AccountId.fromString("0.0.5005").toString()).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnMainnet() {
        SnapshotMatcher.expect(AccountId.fromString("0.0.123-vfmkw").toStringWithChecksum(mainnetClient)).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnTestnet() {
        SnapshotMatcher.expect(AccountId.fromString("0.0.123-rmkyk").toStringWithChecksum(testnetClient)).toMatchSnapshot();
    }

    @Test
    void fromStringWithChecksumOnPreviewnet() {
        SnapshotMatcher.expect(AccountId.fromString("0.0.123-ntjly").toStringWithChecksum(previewnetClient)).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddress() {
        SnapshotMatcher.expect(AccountId.fromSolidityAddress("000000000000000000000000000000000000138D").toString()).toMatchSnapshot();
    }

    @Test
    void fromSolidityAddressWith0x() {
        SnapshotMatcher.expect(AccountId.fromSolidityAddress("0x000000000000000000000000000000000000138D").toString()).toMatchSnapshot();
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(Hex.toHexString(new AccountId(5005).toProtobuf().toByteArray())).toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(AccountId.fromBytes(new AccountId(5005).toBytes()).toString()).toMatchSnapshot();
    }

    @Test
    void toSolidityAddress() {
        SnapshotMatcher.expect(new AccountId(5005).toSolidityAddress()).toMatchSnapshot();
    }
}
