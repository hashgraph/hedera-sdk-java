// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.time.Duration;
import java.util.Objects;
import org.hiero.sdk.java.AccountCreateTransaction;
import org.hiero.sdk.java.AccountDeleteTransaction;
import org.hiero.sdk.java.AccountInfoQuery;
import org.hiero.sdk.java.Hbar;
import org.hiero.sdk.java.PrecheckStatusException;
import org.hiero.sdk.java.PrivateKey;
import org.hiero.sdk.java.ReceiptStatusException;
import org.hiero.sdk.java.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete account")
    void canDeleteAccount() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                    .setKey(key)
                    .setInitialBalance(new Hbar(1))
                    .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

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

            assertThatExceptionOfType(PrecheckStatusException.class)
                    .isThrownBy(() -> {
                        new AccountDeleteTransaction()
                                .setTransferAccountId(testEnv.operatorId)
                                .execute(testEnv.client)
                                .getReceipt(testEnv.client);
                    })
                    .withMessageContaining(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString());
        }
    }

    @Test
    @DisplayName("Cannot delete account that has not signed transaction")
    void cannotDeleteAccountThatHasNotSignedTransaction() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                    .setKey(key)
                    .setInitialBalance(new Hbar(1))
                    .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            assertThatExceptionOfType(ReceiptStatusException.class)
                    .isThrownBy(() -> {
                        new AccountDeleteTransaction()
                                .setAccountId(accountId)
                                .setTransferAccountId(testEnv.operatorId)
                                .execute(testEnv.client)
                                .getReceipt(testEnv.client);
                    })
                    .withMessageContaining(Status.INVALID_SIGNATURE.toString());
        }
    }
}
