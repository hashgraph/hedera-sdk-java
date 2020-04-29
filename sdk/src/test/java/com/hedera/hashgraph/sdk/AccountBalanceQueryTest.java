package com.hedera.hashgraph.sdk;

import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.expect;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;

public class AccountBalanceQueryTest {
    @BeforeAll
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    AccountBalanceQuery setQuery() {
        return new AccountBalanceQuery()
            .setAccountId(AccountId.fromString("0.0.5005"));
    }

    @Test
    @DisplayName("object to be sent matches snapshot")
    void matchesSnap() {
        expect(setQuery().makeRequest()).toMatchSnapshot();
    }
}
