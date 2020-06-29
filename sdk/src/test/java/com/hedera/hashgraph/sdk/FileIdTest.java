package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;

import com.google.protobuf.InvalidProtocolBufferException;

import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FileIdTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerializeFromString() {
        SnapshotMatcher.expect(FileId.fromString("0.0.5005").toProtobuf().toString()).toMatchSnapshot();
    }

    @Test
    void toBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(new FileId(5005).toBytes()).toMatchSnapshot();
    }


    @Test
    void fromBytes() throws InvalidProtocolBufferException {
        SnapshotMatcher.expect(FileId.fromBytes(new FileId(5005).toBytes())).toMatchSnapshot();
    }
}
