package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenRelationshipTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    TokenRelationship spawnTokenRelationshipExample() {
        return new TokenRelationship(
            TokenId.fromString("1.2.3"),
            "ABC",
            55,
            true,
            true,
            true
        );
    }

    @Test
    void shouldSerializeTokenRelationship() throws Exception {
        var originalTokenRelationship = spawnTokenRelationshipExample();
        byte[] tokenRelationshipBytes = originalTokenRelationship.toBytes();
        var copyTokenRelationship = TokenRelationship.fromBytes(tokenRelationshipBytes);
        assertThat(copyTokenRelationship.toString().replaceAll("@[A-Za-z0-9]+", ""))
            .isEqualTo(originalTokenRelationship.toString().replaceAll("@[A-Za-z0-9]+", ""));
        SnapshotMatcher.expect(originalTokenRelationship.toString().replaceAll("@[A-Za-z0-9]+", "")).toMatchSnapshot();
    }
}
