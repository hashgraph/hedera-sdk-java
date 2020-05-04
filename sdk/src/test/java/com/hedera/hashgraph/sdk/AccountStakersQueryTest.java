package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountStakersQueryTest {
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
        SnapshotMatcher.expect(new AccountRecordsQuery()
            .setAccountId(AccountId.fromString("0.0.5005"))
            .setMaxQueryPayment(Hbar.fromTinybars(100_000))
            .toString()
        ).toMatchSnapshot();
    }
}
