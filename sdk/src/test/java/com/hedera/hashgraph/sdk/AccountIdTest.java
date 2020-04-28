package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountIdTest {
    @BeforeAll
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    AccountId setId() {
        return AccountId.fromString("0.0.5005");
    }

    @Test
    @DisplayName("creates account id from string correctly")
    void fromString() {
        AccountId id = setId();
        expect(id).toMatchSnapshot();
    }

    @Test
    @DisplayName("using toBytes and fromBytes will produce the correct Id")
    void toFromBytes() throws InvalidProtocolBufferException {
        byte[] idBytes = setId().toBytes();
        AccountId id = AccountId.fromBytes(idBytes);

        expect(id).toMatchSnapshot();
    }

    @Test
    @DisplayName("incorrect account id from string should fail")
    void badAccountId() {
        expect(assertThrows(IllegalArgumentException.class, () -> AccountId.fromString("a.0.5005")).getMessage()).toMatchSnapshot();
    }
}
