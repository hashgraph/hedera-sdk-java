// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.hiero.sdk.AccountId;
import org.hiero.sdk.Hbar;
import org.hiero.sdk.PrivateKey;
import org.hiero.sdk.TransactionReceiptQuery;
import org.hiero.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountIdPopulationIntegrationTest {
    @Test
    @DisplayName("Can populate AccountId num from mirror node (using sync method)")
    void canPopulateAccountIdNumSync() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction()
                    .addHbarTransfer(evmAddressAccount, new Hbar(1))
                    .addHbarTransfer(testEnv.operatorId, new Hbar(-1))
                    .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                    .setTransactionId(tx.transactionId)
                    .setIncludeChildren(true)
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
        try (var testEnv = new IntegrationTestEnv(1)) {

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction()
                    .addHbarTransfer(evmAddressAccount, new Hbar(1))
                    .addHbarTransfer(testEnv.operatorId, new Hbar(-1))
                    .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                    .setTransactionId(tx.transactionId)
                    .setIncludeChildren(true)
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
        try (var testEnv = new IntegrationTestEnv(1)) {

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction()
                    .addHbarTransfer(evmAddressAccount, new Hbar(1))
                    .addHbarTransfer(testEnv.operatorId, new Hbar(-1))
                    .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                    .setTransactionId(tx.transactionId)
                    .setIncludeChildren(true)
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
        try (var testEnv = new IntegrationTestEnv(1)) {

            var privateKey = PrivateKey.generateECDSA();
            var publicKey = privateKey.getPublicKey();

            var evmAddress = publicKey.toEvmAddress();
            var evmAddressAccount = AccountId.fromEvmAddress(evmAddress);

            var tx = new TransferTransaction()
                    .addHbarTransfer(evmAddressAccount, new Hbar(1))
                    .addHbarTransfer(testEnv.operatorId, new Hbar(-1))
                    .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                    .setTransactionId(tx.transactionId)
                    .setIncludeChildren(true)
                    .execute(testEnv.client);

            var newAccountId = receipt.children.get(0).accountId;

            Thread.sleep(5000);
            var accountId =
                    newAccountId.populateAccountEvmAddressAsync(testEnv.client).get();

            assertThat(evmAddressAccount.evmAddress).isEqualTo(accountId.evmAddress);
        }
    }
}
