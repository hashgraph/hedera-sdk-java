package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ContractLoginfo;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.bouncycastle.util.encoders.Hex;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

public class ContractLogInfoTest {
    private static final ContractLoginfo info = ContractLoginfo.newBuilder()
        .setContractID(new ContractId(10).toProtobuf())
        .setBloom(ByteString.copyFrom("bloom", StandardCharsets.UTF_8))
        .addTopic(ByteString.copyFrom("bloom", StandardCharsets.UTF_8))
        .setData(ByteString.copyFrom("data", StandardCharsets.UTF_8))
        .build();

    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void fromProtobuf() {
        SnapshotMatcher.expect(ContractLogInfo.fromProtobuf(info).toString())
            .toMatchSnapshot();
    }

    @Test
    void toProtobuf() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractLogInfo.fromProtobuf(info).toProtobuf().toString())
            .toMatchSnapshot();
    }

    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(ContractLogInfo.fromBytes(info.toByteArray()).toString())
            .toMatchSnapshot();
    }

    @Test
    void toBytes() {
        SnapshotMatcher.expect(Hex.toHexString(ContractLogInfo.fromProtobuf(info).toBytes()))
            .toMatchSnapshot();
    }
}
