package com.hedera.hashgraph.sdk.integration_tests;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.schedule.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.schedule.ScheduleDeleteTransaction;
import com.hedera.hashgraph.sdk.schedule.ScheduleId;
import com.hedera.hashgraph.sdk.schedule.ScheduleInfoQuery;

import org.junit.jupiter.api.Test;

public class ScheduleTest {
    private final TestEnv testEnv = new TestEnv();

    {
        testEnv.client.setMaxTransactionFee(new Hbar(100));
    }

    @Test
    void testScheduleMultiKey() throws HederaStatusException {
        Ed25519PrivateKey key = Ed25519PrivateKey.generate();

        final Transaction transaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(key.publicKey)
            .build(testEnv.client);

        final Transaction scheduled = new ScheduleCreateTransaction()
            .setTransaction(transaction)
            .setPayerAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey.publicKey)
            .build(testEnv.client);

        final ScheduleId scheduleId = scheduled.execute(testEnv.client).getReceipt(testEnv.client).getScheduleId();

        new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        new ScheduleDeleteTransaction()
            .setScheduleId(scheduleId)
            .build(testEnv.client)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);
    }

    @Test
    void testCreateWithSchedule() throws HederaStatusException {
        Ed25519PrivateKey key = Ed25519PrivateKey.generate();

        final Transaction transaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(key.publicKey)
            .build(testEnv.client);

        ScheduleCreateTransaction tx = transaction.schedule();

        final Transaction scheduled = tx
            .setPayerAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey.publicKey)
            .build(testEnv.client);

        final ScheduleId scheduleId = scheduled.execute(testEnv.client).getReceipt(testEnv.client).getScheduleId();

        new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        new ScheduleDeleteTransaction()
            .setScheduleId(scheduleId)
            .build(testEnv.client)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);
    }
}
