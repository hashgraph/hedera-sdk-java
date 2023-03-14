package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.QueryHeader;
import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenNftInfoQueryTest {
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
        new TokenNftInfoQuery()
            .setNftId(TokenId.fromString("0.0.5005").nft(101))
            .setMaxQueryPayment(Hbar.fromTinybars(100_000))
            .onMakeRequest(builder, QueryHeader.newBuilder().build());
        SnapshotMatcher.expect(builder.build().toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }

    @Test
    void propertiesTest() {
        var tokenId = TokenId.fromString("0.0.5005");
        var query = new TokenNftInfoQuery()
            .byAccountId(AccountId.fromString("0.0.123"))
            .byTokenId(tokenId)
            .setStart(5)
            .setEnd(8)
            .setNftId(tokenId.nft(101))
            .setMaxQueryPayment(Hbar.fromTinybars(100_000));

        assertThat(query.getNftId()).hasToString("0.0.5005/101");
        assertThat(query.getTokenId()).isEqualTo(tokenId);
        assertThat(query.getAccountId()).hasToString("0.0.123");
        assertThat(query.getStart()).isEqualTo(5);
        assertThat(query.getEnd()).isEqualTo(8);
    }
}
