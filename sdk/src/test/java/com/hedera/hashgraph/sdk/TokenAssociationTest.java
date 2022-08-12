package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenAssociationTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    TokenAssociation spawnTokenAssociationExample() {
        return new TokenAssociation(
            TokenId.fromString("1.2.3"),
            AccountId.fromString("4.5.6")
        );
    }

    @Test
    void shouldSerializeAccount() throws Exception {
        var originalTokenAssociation = spawnTokenAssociationExample();
        byte[] tokenAssociationBytes = originalTokenAssociation.toBytes();
        var copyTokenAssociation = TokenAssociation.fromBytes(tokenAssociationBytes);
        assertThat(copyTokenAssociation.toString().replaceAll("@[A-Za-z0-9]+", ""))
            .isEqualTo(originalTokenAssociation.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalTokenAssociation.toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }
}
