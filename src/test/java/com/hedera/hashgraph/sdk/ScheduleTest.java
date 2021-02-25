package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.integration_tests.TestEnv;
import com.hedera.hashgraph.sdk.schedule.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.schedule.ScheduleDeleteTransaction;
import com.hedera.hashgraph.sdk.schedule.ScheduleId;
import com.hedera.hashgraph.sdk.schedule.ScheduleInfo;
import com.hedera.hashgraph.sdk.schedule.ScheduleInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ScheduleTest {
    private final TestEnv testEnv = new TestEnv();

    {
        testEnv.client.setMaxTransactionFee(new Hbar(100));
    }

    @Test
    @DisplayName("Create schedule")
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

        final ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        final Transaction infoTx;
        try{
            infoTx = info.getTransaction();
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        new ScheduleDeleteTransaction()
            .setScheduleId(scheduleId)
            .build(testEnv.client)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);
    }
}
