package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import com.hedera.hashgraph.sdk.proto.ProxyStaker;

public class ProxyStakerTest {
    private static final ProxyStaker proxyStaker = ProxyStaker.newBuilder()
        .setAccountID(new AccountId(100).toProtobuf())
        .setAmount(10)
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
        SnapshotMatcher.expect(com.hedera.hashgraph.sdk.ProxyStaker.fromProtobuf(proxyStaker).toString())
            .toMatchSnapshot();
    }
}
