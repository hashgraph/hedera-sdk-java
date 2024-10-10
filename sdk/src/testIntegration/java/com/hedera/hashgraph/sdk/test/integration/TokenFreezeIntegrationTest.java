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
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenFreezeTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenFreezeIntegrationTest {
    @Test
    @DisplayName("Can freeze account with token")
    void canFreezeAccountWithToken() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(testEnv.operatorKey)
                    .setFreezeKey(testEnv.operatorKey)
                    .setWipeKey(testEnv.operatorKey)
                    .setKycKey(testEnv.operatorKey)
                    .setSupplyKey(testEnv.operatorKey)
                    .setFreezeDefault(false)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenFreezeTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Cannot freeze account on token when token ID is not set")
    void cannotFreezeAccountOnTokenWhenTokenIDIsNotSet() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                new TokenFreezeTransaction()
                    .setAccountId(accountId)
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_TOKEN_ID.toString());

        }  
    }

    @Test
    @DisplayName("Cannot freeze account on token when account ID is not set")
    void cannotFreezeAccountOnTokenWhenAccountIDIsNotSet() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                new TokenFreezeTransaction()
                    .setTokenId(tokenId)
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_ACCOUNT_ID.toString());

        }
    }

    @Test
    @DisplayName("Cannot freeze account on token when account was not associated with")
    void cannotFreezeAccountOnTokenWhenAccountWasNotAssociatedWith() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(testEnv.operatorKey)
                    .setFreezeKey(testEnv.operatorKey)
                    .setWipeKey(testEnv.operatorKey)
                    .setKycKey(testEnv.operatorKey)
                    .setSupplyKey(testEnv.operatorKey)
                    .setFreezeDefault(false)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                new TokenFreezeTransaction()
                    .setAccountId(accountId)
                    .setTokenId(tokenId)
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.TOKEN_NOT_ASSOCIATED_TO_ACCOUNT.toString());

        }
    }
}
