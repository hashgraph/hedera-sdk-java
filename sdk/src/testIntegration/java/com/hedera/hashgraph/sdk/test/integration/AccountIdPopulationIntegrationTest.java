package com.hedera.hashgraph.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountIdPopulationIntegrationTest {
    @Test
    @DisplayName("Can populate AccountId num from mirror node (using sync method)")
    void canPopulateAccountIdNumSync() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

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

    @Test
    @DisplayName("Can populate AccountId num from mirror node (using async method)")
    void canPopulateAccountIdNumAsync() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

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

    @Test
    @DisplayName("Can populate AccountId evm address from mirror node (using sync method)")
    void canPopulateAccountIdEvmAddressSync() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

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

    @Test
    @DisplayName("Can populate AccountId evm address from mirror node (using async method)")
    void canPopulateAccountIdEvmAddressAsync() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

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
