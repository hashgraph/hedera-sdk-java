package com.hedera.hashgraph.sdk.integration_tests;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.account.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.TransferTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicCreateTransaction;
import com.hedera.hashgraph.sdk.consensus.ConsensusTopicId;
import com.hedera.hashgraph.sdk.crypto.KeyList;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.schedule.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ScheduleTest {
    private final TestEnv testEnv = new TestEnv();

    {
        testEnv.client.setMaxTransactionFee(new Hbar(100));
    }

    @Test
    @Disabled
    @DisplayName("Create schedule")
    void testScheduleMultiKey() throws HederaStatusException {
        Ed25519PrivateKey key = Ed25519PrivateKey.generate();

        final Transaction transaction = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(key.publicKey)
            .build(testEnv.client);

        final ScheduleCreateTransaction scheduled = new ScheduleCreateTransaction()
            .setPayerAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey.publicKey);

        try{
            scheduled.setScheduledTransaction(transaction);
        }catch(InvalidProtocolBufferException e){
            System.out.println(e.toString());
        }

        final Transaction scheduledTransaction = scheduled
            .build(testEnv.client);

        final ScheduleId scheduleId = scheduledTransaction.execute(testEnv.client).getReceipt(testEnv.client).getScheduleId();

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
    @Disabled
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
    void testScheduleCryptoTransfer() throws HederaStatusException, InvalidProtocolBufferException, InterruptedException, TimeoutException {
        Ed25519PrivateKey key1 = Ed25519PrivateKey.generate();
        Ed25519PrivateKey key2 = Ed25519PrivateKey.generate();
        Ed25519PrivateKey key3 = Ed25519PrivateKey.generate();

        KeyList keyList = new KeyList();

        keyList.add(key1.publicKey);
        keyList.add(key2.publicKey);
        keyList.add(key3.publicKey);

        // Creat the account with the `KeyList`
        TransactionId transactionId = new AccountCreateTransaction()
            .setNodeAccountId(new AccountId(3))
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionId.getReceipt(testEnv.client);

        AccountId accountId = Objects.requireNonNull(receipt.getAccountId());

        // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
        // after we expect it to have been executed
        transactionId = new TransactionId(testEnv.operatorId);

        // Create a transfer transaction with 2/3 signatures.
        TransferTransaction transfer = new TransferTransaction()
            .setTransactionId(transactionId)
            .addHbarTransfer(accountId, new Hbar(1).negate())
            .addHbarTransfer(testEnv.operatorId, new Hbar(1));

        // Schedule the transactoin
        ScheduleCreateTransaction scheduled = transfer.build(testEnv.client).schedule();

        receipt = scheduled.execute(testEnv.client).getReceipt(testEnv.client);

        // Get the schedule ID from the receipt
        ScheduleId scheduleId = Objects.requireNonNull(receipt.getScheduleId());

        // Get the schedule info to see if `signatories` is populated with 2/3 signatures
        ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertNull(info.executionTime);

        // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
        // since all 3 signatures have been provided.
        Transaction transaction = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .build(testEnv.client);

        transaction.sign(key1).sign(key2).sign(key3).execute(testEnv.client).getReceipt(testEnv.client);

        info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertNotNull(info.executionTime);
    }

    @Test
    void testScheduleConsensusMessageSubmit() throws HederaStatusException, InvalidProtocolBufferException, InterruptedException, TimeoutException {
        Ed25519PrivateKey key = Ed25519PrivateKey.generate();

        // Creat the account with the `KeyList`
        TransactionId transactionId = new ConsensusTopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey.publicKey)
            .setSubmitKey(key.publicKey)
            .execute(testEnv.client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionId.getReceipt(testEnv.client);

        ConsensusTopicId topicId = Objects.requireNonNull(receipt.getConsensusTopicId());

        // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
        // after we expect it to have been executed
        transactionId = new TransactionId(testEnv.operatorId);

        // Create a transfer transaction with 2/3 signatures.
        ConsensusMessageSubmitTransaction transfer = new ConsensusMessageSubmitTransaction()
            .setTransactionId(transactionId)
            .setTopicId(topicId)
            .setMessage("Hello, scheduled transaction");

        // Schedule the transactoin
        ScheduleCreateTransaction scheduled = new ArrayList<>(transfer.build(testEnv.client).getTransactions())
            .get(0).schedule();

        receipt = scheduled.execute(testEnv.client).getReceipt(testEnv.client);

        // Get the schedule ID from the receipt
        ScheduleId scheduleId = Objects.requireNonNull(receipt.getScheduleId());

        // Get the schedule info to see if `signatories` is populated with 2/3 signatures
        ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertNull(info.executionTime);

        // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
        // since all 3 signatures have been provided.
        Transaction transaction = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .build(testEnv.client);

        transaction.sign(key).execute(testEnv.client).getReceipt(testEnv.client);

        info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertNotNull(info.executionTime);
    }
}
