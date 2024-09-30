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
package com.hedera.hashgraph.sdk.test.integration;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class AccountUpdateIntegrationTest {
    @Test
    @DisplayName("Can update account with a new key")
    void canUpdateAccountWithNewKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key1 = PrivateKey.generateED25519();
        var key2 = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key1)
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        var info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(info.accountId).isEqualTo(accountId);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.key.toString()).isEqualTo(key1.getPublicKey().toString());
        assertThat(info.balance).isEqualTo(new Hbar(0));
        assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
        assertThat(info.proxyAccountId).isNull();
        assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);

        new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setKey(key2.getPublicKey())
            .freezeWith(testEnv.client)
            .sign(key1)
            .sign(key2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(info.accountId).isEqualTo(accountId);
        assertThat(info.isDeleted).isFalse();
        assertThat(info.key.toString()).isEqualTo(key2.getPublicKey().toString());
        assertThat(info.balance).isEqualTo(new Hbar(0));
        assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
        assertThat(info.proxyAccountId).isNull();
        assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);

        testEnv.close(accountId, key2);
    }

    @Test
    @DisplayName("Cannot update account when account ID is not set")
    void cannotUpdateAccountWhenAccountIdIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new AccountUpdateTransaction()
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString());

        testEnv.close();
    }
}
