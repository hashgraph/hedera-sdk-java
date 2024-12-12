/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2021 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hiero.sdk.AccountBalanceQuery;
import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.AccountDeleteTransaction;
import com.hiero.sdk.AccountId;
import com.hiero.sdk.AccountUpdateTransaction;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.KeyList;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.ReceiptStatusException;
import com.hiero.sdk.ScheduleCreateTransaction;
import com.hiero.sdk.ScheduleId;
import com.hiero.sdk.ScheduleInfo;
import com.hiero.sdk.ScheduleInfoQuery;
import com.hiero.sdk.ScheduleSignTransaction;
import com.hiero.sdk.Status;
import com.hiero.sdk.TokenAssociateTransaction;
import com.hiero.sdk.TokenCreateTransaction;
import com.hiero.sdk.TopicCreateTransaction;
import com.hiero.sdk.TopicMessageSubmitTransaction;
import com.hiero.sdk.TransactionId;
import com.hiero.sdk.TransactionReceipt;
import com.hiero.sdk.TransactionResponse;
import com.hiero.sdk.TransferTransaction;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ScheduleCreateIntegrationTest {

    private final int oneDayInSecs = 86400;

    @Test
    @Disabled
    @DisplayName("Can create schedule")
    void canCreateSchedule() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info.executedAt).isNotNull();

        }
    }

    @Test
    @Disabled
    @DisplayName("Can get Transaction")
    void canGetTransactionSchedule() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info.executedAt).isNotNull();
            assertThat(info.getScheduledTransaction()).isNotNull();

        }
    }

    @Test
    @Disabled
    @DisplayName("Can create schedule with schedule()")
    void canCreateWithSchedule() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10));

            var tx = transaction.schedule();

            var response = tx
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info.executedAt).isNotNull();
            assertThat(info.getScheduledTransaction()).isNotNull();

        }
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule2() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key1 = PrivateKey.generateED25519();
            PrivateKey key2 = PrivateKey.generateED25519();
            PrivateKey key3 = PrivateKey.generateED25519();

            KeyList keyList = new KeyList();

            keyList.add(key1.getPublicKey());
            keyList.add(key2.getPublicKey());
            keyList.add(key3.getPublicKey());

            // Creat the account with the `KeyList`
            TransactionResponse response = new AccountCreateTransaction()
                .setKey(keyList)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client);

            // This will wait for the receipt to become available
            TransactionReceipt receipt = response.getReceipt(testEnv.client);

            AccountId accountId = Objects.requireNonNull(receipt.accountId);

            // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
            // after we expect it to have been executed
            TransactionId transactionId = TransactionId.generate(testEnv.operatorId);

            // Create a transfer transaction with 2/3 signatures.
            TransferTransaction transfer = new TransferTransaction()
                .setTransactionId(transactionId)
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            ScheduleCreateTransaction scheduled = transfer.schedule();

            receipt = scheduled.execute(testEnv.client).getReceipt(testEnv.client);

            // Get the schedule ID from the receipt
            ScheduleId scheduleId = Objects.requireNonNull(receipt.scheduleId);

            // Get the schedule info to see if `signatories` is populated with 2/3 signatures
            ScheduleInfo info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info.executedAt).isNull();

            // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
            // since all 3 signatures have been provided.
            ScheduleSignTransaction signTransaction = new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client);

            signTransaction.sign(key1).sign(key2).sign(key3).execute(testEnv.client).getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info.executedAt).isNotNull();

            assertThat(scheduleId.getChecksum()).isNull();
            assertThat(scheduleId.hashCode()).isNotZero();
            assertThat(scheduleId.compareTo(ScheduleId.fromBytes(scheduleId.toBytes()))).isZero();

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client)
                .sign(key1).sign(key2).sign(key3)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can schedule token transfer")
    void canScheduleTokenTransfer() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()) {

            PrivateKey key = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setReceiverSignatureRequired(true)
                .setKey(key)
                .setInitialBalance(new Hbar(10))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            Objects.requireNonNull(accountId);

            var tokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setInitialSupply(100)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            Objects.requireNonNull(tokenId);

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var scheduleId = new TransferTransaction()
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .schedule()
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            Objects.requireNonNull(scheduleId);

            var balanceQuery1 = new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            assertThat(balanceQuery1.tokens.get(tokenId)).isEqualTo(0);

            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var balanceQuery2 = new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            assertThat(balanceQuery2.tokens.get(tokenId)).isEqualTo(10);

        }
    }

    @Test
    @DisplayName("Cannot schedule two identical transactions")
    void cannotScheduleTwoTransactions() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();
            var accountId = new AccountCreateTransaction()
                .setInitialBalance(new Hbar(10))
                .setKey(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var transferTx = new TransferTransaction()
                .addHbarTransfer(testEnv.operatorId, new Hbar(-10))
                .addHbarTransfer(accountId, new Hbar(10));

            var scheduleId1 = transferTx.schedule()
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            var info1 = new ScheduleInfoQuery()
                .setScheduleId(scheduleId1)
                .execute(testEnv.client);

            assertThat(info1.executedAt).isNotNull();

            var transferTxFromInfo = info1.getScheduledTransaction();

            var scheduleCreateTx1 = transferTx.schedule();
            var scheduleCreateTx2 = transferTxFromInfo.schedule();

            assertThat(scheduleCreateTx2.toString()).isEqualTo(scheduleCreateTx1.toString());

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                transferTxFromInfo.schedule()
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining("IDENTICAL_SCHEDULE_ALREADY_CREATED");

        }
    }

    @Test
    @DisplayName("Can schedule topic message")
    void canScheduleTopicMessage() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            // Generate 3 random keys
            var key1 = PrivateKey.generateED25519();

            // This is the submit key
            var key2 = PrivateKey.generateED25519();

            var key3 = PrivateKey.generateED25519();

            var keyList = new KeyList();

            keyList.add(key1.getPublicKey());
            keyList.add(key2.getPublicKey());
            keyList.add(key3.getPublicKey());

            var response = new AccountCreateTransaction()
                .setInitialBalance(new Hbar(100))
                .setKey(keyList)
                .execute(testEnv.client);

            assertThat(response.getReceipt(testEnv.client).accountId).isNotNull();

            var topicId = Objects.requireNonNull(new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setAutoRenewAccountId(testEnv.operatorId)
                .setTopicMemo("HCS Topic_")
                .setSubmitKey(key2.getPublicKey())
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .topicId
            );

            var transaction = new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("scheduled hcs message".getBytes(StandardCharsets.UTF_8));

            // create schedule
            var scheduledTx = transaction.schedule()
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .setScheduleMemo("mirror scheduled E2E signature on create and sign_" + Instant.now());

            var scheduled = scheduledTx.freezeWith(testEnv.client);

            var scheduleId = Objects.requireNonNull(scheduled
                .execute(testEnv.client)
                .getReceipt(testEnv.client).scheduleId);

            // verify schedule has been created and has 1 of 2 signatures
            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info).isNotNull();
            assertThat(info.scheduleId).isEqualTo(scheduleId);

            var infoTransaction = (TopicMessageSubmitTransaction) info.getScheduledTransaction();

            assertThat(transaction.getTopicId()).isEqualTo(infoTransaction.getTopicId());
            assertThat(transaction.getNodeAccountIds()).isEqualTo(infoTransaction.getNodeAccountIds());

            var scheduleSign = new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client);

            scheduleSign
                .sign(key2)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertThat(info.executedAt).isNotNull();

        }
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setKey(key.getPublicKey())
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            var scheduleId = transfer
                .schedule()
                .setExpirationTime(Instant.now().plusSeconds(oneDayInSecs))
                .setScheduleMemo("HIP-423 Integration Test")
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            ScheduleInfo info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is not yet executed
            assertThat(info.executedAt).isNull();

            // Schedule sign
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is executed
            assertThat(info.executedAt).isNotNull();

            assertThat(scheduleId.getChecksum()).isNull();
            assertThat(scheduleId.hashCode()).isNotZero();
            assertThat(scheduleId.compareTo(ScheduleId.fromBytes(scheduleId.toBytes()))).isZero();
        }
    }

    @Test
    @DisplayName("Cannot schedule one year into the future")
    void cannotScheduleTransactionOneYearIntoTheFuture() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setKey(key.getPublicKey())
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() ->
                    transfer
                        .schedule()
                        .setExpirationTime(Instant.now().plus(Duration.ofDays(365)))
                        .setScheduleMemo("HIP-423 Integration Test")
                        .execute(testEnv.client)
                        .getReceipt(testEnv.client))
                .withMessageContaining(Status.SCHEDULE_EXPIRATION_TIME_TOO_FAR_IN_FUTURE.toString());
        }
    }

    @Test
    @DisplayName("Cannot schedule in the past")
    void cannotScheduleTransactionInThePast() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setKey(key.getPublicKey())
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() ->
                    transfer
                        .schedule()
                        .setExpirationTime(Instant.now().minusSeconds(10))
                        .setScheduleMemo("HIP-423 Integration Test")
                        .execute(testEnv.client)
                        .getReceipt(testEnv.client))
                .withMessageContaining(Status.SCHEDULE_EXPIRATION_TIME_MUST_BE_HIGHER_THAN_CONSENSUS_TIME.toString());
        }
    }

    @Test
    @DisplayName("Can sign schedule and wait for expiry")
    void canSignScheduleAndWaitForExpiry() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setKey(key.getPublicKey())
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            var scheduleId = transfer
                .schedule()
                .setExpirationTime(Instant.now().plusSeconds(oneDayInSecs))
                .setWaitForExpiry(true)
                .setScheduleMemo("HIP-423 Integration Test")
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            ScheduleInfo info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is not yet executed
            assertThat(info.executedAt).isNull();

            // Schedule sign
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is still not executed
            assertThat(info.executedAt).isNull();

            assertThat(scheduleId.getChecksum()).isNull();
            assertThat(scheduleId.hashCode()).isNotZero();
            assertThat(scheduleId.compareTo(ScheduleId.fromBytes(scheduleId.toBytes()))).isZero();
        }
    }

    @Test
    @DisplayName("Can sign with multisig and update signing requirements")
    void canSignWithMultiSigAndUpdateSigningRequirements() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key1 = PrivateKey.generateED25519();
            PrivateKey key2 = PrivateKey.generateED25519();
            PrivateKey key3 = PrivateKey.generateED25519();
            PrivateKey key4 = PrivateKey.generateED25519();

            KeyList keyList = KeyList.withThreshold(2);

            keyList.add(key1.getPublicKey());
            keyList.add(key2.getPublicKey());
            keyList.add(key3.getPublicKey());

            var accountId = new AccountCreateTransaction()
                .setKey(keyList)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            var scheduleId = transfer
                .schedule()
                .setExpirationTime(Instant.now().plusSeconds(oneDayInSecs))
                .setScheduleMemo("HIP-423 Integration Test")
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            ScheduleInfo info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is not executed
            assertThat(info.executedAt).isNull();

            // Sign with one key
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is still not executed
            assertThat(info.executedAt).isNull();

            // Update the signing requirements
            new AccountUpdateTransaction()
                .setAccountId(accountId)
                .setKey(key4.getPublicKey())
                .freezeWith(testEnv.client)
                .sign(key1)
                .sign(key2)
                .sign(key4)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is still not executed
            assertThat(info.executedAt).isNull();

            // Sign with the updated key
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key4)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is executed
            assertThat(info.executedAt).isNotNull();
        }
    }

    @Test
    @DisplayName("Can sign with multisig")
    void canSignWithMultiSig() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key1 = PrivateKey.generateED25519();
            PrivateKey key2 = PrivateKey.generateED25519();
            PrivateKey key3 = PrivateKey.generateED25519();

            KeyList keyList = KeyList.withThreshold(2);

            keyList.add(key1.getPublicKey());
            keyList.add(key2.getPublicKey());
            keyList.add(key3.getPublicKey());

            var accountId = new AccountCreateTransaction()
                .setKey(keyList)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            var scheduleId = transfer
                .schedule()
                .setExpirationTime(Instant.now().plusSeconds(oneDayInSecs))
                .setScheduleMemo("HIP-423 Integration Test")
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            ScheduleInfo info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is not executed
            assertThat(info.executedAt).isNull();

            // Sign with one key
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is still not executed
            assertThat(info.executedAt).isNull();

            // Update the signing requirements
            new AccountUpdateTransaction()
                .setAccountId(accountId)
                .setKey(key1.getPublicKey())
                .freezeWith(testEnv.client)
                .sign(key1)
                .sign(key2)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is still not executed
            assertThat(info.executedAt).isNull();

            // Sign with one more key
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key2)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is executed
            assertThat(info.executedAt).isNotNull();
        }
    }

    @Test
    @DisplayName("Can execute with short expiration time")
    void canExecuteWithShortExpirationTime() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            PrivateKey key1 = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setKey(key1)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            // Create the transaction
            TransferTransaction transfer = new TransferTransaction()
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transaction
            var scheduleId = transfer
                .schedule()
                .setExpirationTime(Instant.now().plusSeconds(10))
                .setWaitForExpiry(true)
                .setScheduleMemo("HIP-423 Integration Test")
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId;

            ScheduleInfo info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is not executed
            assertThat(info.executedAt).isNull();

            // Sign
            new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            // Verify the transaction is still not executed
            assertThat(info.executedAt).isNull();

            var accountBalanceBefore = new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            Thread.sleep(10_000);

            var accountBalanceAfter = new AccountBalanceQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            // Verify the transaction executed after 10 seconds
            assertThat(accountBalanceBefore.hbars.compareTo(accountBalanceAfter.hbars)).isEqualTo(1);
        }
    }
}
