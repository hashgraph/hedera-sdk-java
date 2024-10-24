/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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

import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.AccountDeleteTransaction;
import com.hiero.sdk.AccountInfoQuery;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.PrecheckStatusException;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.ReceiptStatusException;
import com.hiero.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete account")
    void canDeleteAccount() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            assertThat(info.accountId).isEqualTo(accountId);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.key.toString()).isEqualTo(key.getPublicKey().toString());
            assertThat(info.balance).isEqualTo(new Hbar(1));
            assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
            assertThat(info.proxyAccountId).isNull();
            assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);

        }
    }

    @Test
    @DisplayName("Cannot delete invalid account ID")
    void cannotCreateAccountWithNoKey() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                new AccountDeleteTransaction()
                    .setTransferAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString());

        }
    }

    @Test
    @DisplayName("Cannot delete account that has not signed transaction")
    void cannotDeleteAccountThatHasNotSignedTransaction() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                new AccountDeleteTransaction()
                    .setAccountId(accountId)
                    .setTransferAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_SIGNATURE.toString());

        }
    }
}
