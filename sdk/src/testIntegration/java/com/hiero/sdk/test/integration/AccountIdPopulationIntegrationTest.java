/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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

import com.hiero.sdk.AccountId;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.TransactionReceiptQuery;
import com.hiero.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountIdPopulationIntegrationTest {
    @Test
    @DisplayName("Can populate AccountId num from mirror node (using sync method)")
    void canPopulateAccountIdNumSync() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction().addHbarTransfer(evmAddressAccount, new Hbar(1))
                .addHbarTransfer(testEnv.operatorId, new Hbar(-1)).execute(testEnv.client);

            var receipt = new TransactionReceiptQuery().setTransactionId(tx.transactionId).setIncludeChildren(true)
                .execute(testEnv.client);

            var newAccountId = receipt.children.get(0).accountId;

            var idMirror = AccountId.fromEvmAddress(evmAddress);
            Thread.sleep(5000);
            var accountId = idMirror.populateAccountNum(testEnv.client);

            assertThat(newAccountId.num).isEqualTo(accountId.num);
        }
    }

    @Test
    @DisplayName("Can populate AccountId num from mirror node (using async method)")
    void canPopulateAccountIdNumAsync() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction().addHbarTransfer(evmAddressAccount, new Hbar(1))
                .addHbarTransfer(testEnv.operatorId, new Hbar(-1)).execute(testEnv.client);

            var receipt = new TransactionReceiptQuery().setTransactionId(tx.transactionId).setIncludeChildren(true)
                .execute(testEnv.client);

            var newAccountId = receipt.children.get(0).accountId;

            var idMirror = AccountId.fromEvmAddress(evmAddress);
            Thread.sleep(5000);
            var accountId = idMirror.populateAccountNumAsync(testEnv.client).get();

            assertThat(newAccountId.num).isEqualTo(accountId.num);

            }
    }

    @Test
    @DisplayName("Can populate AccountId evm address from mirror node (using sync method)")
    void canPopulateAccountIdEvmAddressSync() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction().addHbarTransfer(evmAddressAccount, new Hbar(1))
                .addHbarTransfer(testEnv.operatorId, new Hbar(-1)).execute(testEnv.client);

            var receipt = new TransactionReceiptQuery().setTransactionId(tx.transactionId).setIncludeChildren(true)
                .execute(testEnv.client);

            var newAccountId = receipt.children.get(0).accountId;

            Thread.sleep(5000);
            var accountId = newAccountId.populateAccountEvmAddress(testEnv.client);

            assertThat(evmAddressAccount.evmAddress).isEqualTo(accountId.evmAddress);
        }
    }

    @Test
    @DisplayName("Can populate AccountId evm address from mirror node (using async method)")
    void canPopulateAccountIdEvmAddressAsync() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction().addHbarTransfer(evmAddressAccount, new Hbar(1))
                .addHbarTransfer(testEnv.operatorId, new Hbar(-1)).execute(testEnv.client);

            var receipt = new TransactionReceiptQuery().setTransactionId(tx.transactionId).setIncludeChildren(true)
                .execute(testEnv.client);

            var newAccountId = receipt.children.get(0).accountId;

            Thread.sleep(5000);
            var accountId = newAccountId.populateAccountEvmAddressAsync(testEnv.client).get();

            assertThat(evmAddressAccount.evmAddress).isEqualTo(accountId.evmAddress);
        }
    }

}
