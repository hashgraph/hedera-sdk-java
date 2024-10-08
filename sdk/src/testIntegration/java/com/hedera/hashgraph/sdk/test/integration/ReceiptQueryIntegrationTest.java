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
package com.hedera.hashgraph.sdk.test.integration;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import com.hedera.hashgraph.sdk.TransactionRecordQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ReceiptQueryIntegrationTest {
    @Test
    @DisplayName("Can get Receipt")
    void canGetTransactionReceipt() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can get Record")
    void canGetTransactionRecord() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){
            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

            new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can get Record cost")
    @SuppressWarnings("UnusedVariable")
    void getCostTransactionRecord() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){
            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId);

            recordQuery.getCost(testEnv.client);
            recordQuery.execute(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can get Record cost with big max set")
    @SuppressWarnings("UnusedVariable")
    void getCostBigMaxTransactionRecord() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){
            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setMaxQueryPayment(new Hbar(1000));

            recordQuery.getCost(testEnv.client);

            recordQuery.execute(testEnv.client);

        }
    }

    @Test
    @DisplayName("Error at very small max, getRecord")
    void getCostSmallMaxTransactionRecord() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){
            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = recordQuery.getCost(testEnv.client);

            assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> {
                recordQuery.execute(testEnv.client);
            }).withMessage("cost for TransactionRecordQuery, of " + cost.toString() + ", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

        }
    }

    @Test
    @DisplayName("Insufficient transaction fee error for transaction record query")
    void getCostInsufficientTxFeeTransactionRecord() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){
            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId);

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                recordQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
            }).satisfies(error -> assertThat(error.status.toString()).isEqualTo("INSUFFICIENT_TX_FEE"));

        }
    }
}
