package com.hedera.hashgraph.sdk;

import io.github.jsonSnapshot.SnapshotMatcher;
import org.junit.AfterClass;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountBalanceQueryTest {
    @BeforeAll
    public static void beforeAll() {
        SnapshotMatcher.start();
    }

    @AfterClass
    public static void afterAll() {
        SnapshotMatcher.validateSnapshots();
    }

    @Test
    void shouldSerializeWithAccount() {
        SnapshotMatcher.expect(new AccountBalanceQuery()
            .setAccountId(AccountId.fromString("0.0.5005"))
            .toString()
        ).toMatchSnapshot();
    }

    @Test
    void shouldSerializeWithContract() {
        SnapshotMatcher.expect(new AccountBalanceQuery()
            .setContractId(ContractId.fromString("0.0.5005"))
            .toString()
        ).toMatchSnapshot();
    }
}
