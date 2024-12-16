// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TransactionResponseTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the transaction response transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheTransactionResponseTransactionHash() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var transaction = new AccountCreateTransaction().setKey(key).execute(testEnv.client);

            var record = transaction.getRecord(testEnv.client);

            assertThat(record.transactionHash.toByteArray()).containsExactly(transaction.transactionHash);

            var accountId = record.receipt.accountId;
            assertThat(accountId).isNotNull();
        }
    }
}
