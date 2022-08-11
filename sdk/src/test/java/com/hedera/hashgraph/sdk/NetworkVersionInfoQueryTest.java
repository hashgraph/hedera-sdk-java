package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Transaction;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NetworkVersionInfoQueryTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerialize() {
        var builder = com.hedera.hashgraph.sdk.proto.Query.newBuilder();
        new NetworkVersionInfoQuery()
            .setMaxQueryPayment(Hbar.fromTinybars(100_000))
            .onMakeRequest(
                builder,
                QueryHeader
                    .newBuilder()
                    .setPayment(
                        Transaction.newBuilder()
                            .setSignedTransactionBytes(
                                ByteString.fromHex("deadbeef")
                            ).build()
                    ).build()
            );
        SnapshotMatcher.expect(builder.build().toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }
}
