package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.TransactionID;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.TransferTransaction;
import com.hedera.hashgraph.sdk.crypto.KeyList;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.integration_tests.TestEnv;
import com.hedera.hashgraph.sdk.schedule.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.schedule.ScheduleDeleteTransaction;
import com.hedera.hashgraph.sdk.schedule.ScheduleId;
import com.hedera.hashgraph.sdk.schedule.ScheduleInfo;
import com.hedera.hashgraph.sdk.schedule.ScheduleInfoQuery;
import com.hedera.hashgraph.sdk.schedule.ScheduleSignTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

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

        ScheduleCreateTransaction scheduled = new ScheduleCreateTransaction()
            .setPayerAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey.publicKey);

        try{
            scheduled = scheduled.setScheduledTransaction(transaction);
        }catch(InvalidProtocolBufferException e){
            System.out.println(e.toString());
        }

        final Transaction scheduledTransaction = scheduled.build(testEnv.client);

        final ScheduleId scheduleId = scheduledTransaction.execute(testEnv.client).getReceipt(testEnv.client).getScheduleId();

        final ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        final Transaction infoTx;
        try{
            infoTx = info.getTransaction();
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        Assertions.assertNotNull(info.executionTime);
    }

    @Test
    @DisplayName("Create with schedule()")
    void testCreateWithSchedule() throws HederaStatusException {
        Ed25519PrivateKey key = Ed25519PrivateKey.generate();

        final Transaction transaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(key.publicKey)
            .build(testEnv.client);

        ScheduleCreateTransaction tx = new ScheduleCreateTransaction();
        try{
            tx = transaction.schedule();
        }catch(InvalidProtocolBufferException e){
            System.out.println(e.toString());
        }

        final Transaction scheduled = tx
            .setPayerAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey.publicKey)
            .build(testEnv.client);

        final ScheduleId scheduleId = scheduled.execute(testEnv.client).getReceipt(testEnv.client).getScheduleId();

        ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        final Transaction infoTx;
        try{
            infoTx = info.getTransaction();
        } catch(Exception e){
            System.out.println(e.getMessage());
        }

        Assertions.assertNotNull(info.executionTime);
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule2() throws HederaStatusException {
        Ed25519PrivateKey key1 = Ed25519PrivateKey.generate();
        Ed25519PrivateKey key2 = Ed25519PrivateKey.generate();
        Ed25519PrivateKey key3 = Ed25519PrivateKey.generate();

        KeyList keyList = new KeyList();

        keyList.add(key1.publicKey);
        keyList.add(key2.publicKey);
        keyList.add(key3.publicKey);
        // Creat the account with the `KeyList`
        TransactionId response = new AccountCreateTransaction()
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = response.getReceipt(testEnv.client);
        AccountId accountId = Objects.requireNonNull(receipt.getAccountId());

        // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
        // after we expect it to have been executed
        TransactionId transactionId = new TransactionId(testEnv.client.getOperatorId());

        // Create a transfer transaction with 2/3 signatures.
        Transaction transfer = new TransferTransaction()
            .setTransactionId(transactionId)
            .addHbarTransfer(accountId, new Hbar(1).negate())
            .addHbarTransfer(testEnv.client.getOperatorId(), new Hbar(1))
            .build(testEnv.client);

        // Schedule the transactoin
        ScheduleCreateTransaction scheduled = new ScheduleCreateTransaction();
        try{
            scheduled = transfer.schedule();
        }catch(InvalidProtocolBufferException e){
            System.out.println(e.toString());
        }

        receipt = scheduled.execute(testEnv.client).getReceipt(testEnv.client);

        // Get the schedule ID from the receipt
        ScheduleId scheduleId = Objects.requireNonNull(receipt.getScheduleId());
        // Get the schedule info to see if `signatories` is populated with 2/3 signatures

        ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);
        // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
        // since all 3 signatures have been provided.

        Transaction signTransaction = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .build(testEnv.client);

        signTransaction.sign(key1).sign(key2).sign(key3).execute(testEnv.client).getReceipt(testEnv.client);

        info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        Assertions.assertNotNull(info.executionTime);
    }
}
