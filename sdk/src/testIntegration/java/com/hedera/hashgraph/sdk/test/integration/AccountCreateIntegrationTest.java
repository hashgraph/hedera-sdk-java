// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hedera.hashgraph.sdk.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AccountCreateIntegrationTest {
    @Test
    @DisplayName("Can create account with only initial balance and key")
    void canCreateAccountWithOnlyInitialBalanceAndKey() throws Exception {
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
    @DisplayName("Can create account with no initial balance")
    void canCreateAccountWithNoInitialBalance() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction().setKey(key).execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

            assertThat(info.accountId).isEqualTo(accountId);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.key.toString()).isEqualTo(key.getPublicKey().toString());
            assertThat(info.balance).isEqualTo(new Hbar(0));
            assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
            assertThat(info.proxyAccountId).isNull();
            assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);
        }
    }

    @Test
    @DisplayName("Cannot create account with no key")
    void canNotCreateAccountWithNoKey() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            assertThatExceptionOfType(PrecheckStatusException.class)
                    .isThrownBy(() -> new AccountCreateTransaction()
                            .setInitialBalance(new Hbar(1))
                            .execute(testEnv.client)
                            .getReceipt(testEnv.client))
                    .withMessageContaining(Status.KEY_REQUIRED.toString());
        }
    }

    @Test
    @DisplayName("Can create account using aliasKey")
    void canCreateWithAliasKey() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var aliasId = key.toAccountId(0, 0);

            new TransferTransaction()
                    .addHbarTransfer(testEnv.operatorId, new Hbar(10).negated())
                    .addHbarTransfer(aliasId, new Hbar(10))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            var info = new AccountInfoQuery().setAccountId(aliasId).execute(testEnv.client);

            assertThat(key.getPublicKey()).isEqualTo(info.aliasKey);
        }
    }

    @Test
    @DisplayName("Regenerates TransactionIds in response to expiration")
    void managesExpiration() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                    .setKey(key)
                    .setTransactionId(
                            new TransactionId(testEnv.operatorId, Instant.now().minusSeconds(40)))
                    .setTransactionValidDuration(Duration.ofSeconds(30))
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

            assertThat(info.accountId).isEqualTo(accountId);
            assertThat(info.isDeleted).isFalse();
            assertThat(info.key.toString()).isEqualTo(key.getPublicKey().toString());
            assertThat(info.balance).isEqualTo(new Hbar(0));
            assertThat(info.autoRenewPeriod).isEqualTo(Duration.ofDays(90));
            assertThat(info.proxyAccountId).isNull();
            assertThat(info.proxyReceived).isEqualTo(Hbar.ZERO);
        }
    }

    @Test
    @DisplayName("Can create account with alias from admin key")
    void createAccountWithAliasFromAdminKey() throws Exception {
        // Tests the third row of this table
        // https://github.com/hashgraph/hedera-improvement-proposal/blob/d39f740021d7da592524cffeaf1d749803798e9a/HIP/hip-583.md#signatures
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateECDSA();
            var evmAddress = adminKey.getPublicKey().toEvmAddress();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var accountId = new AccountCreateTransaction()
                    .setKey(adminKey)
                    .setAlias(evmAddress)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .accountId;

            assertThat(accountId).isNotNull();

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

            assertThat(info.accountId).isNotNull();
            assertThat(info.contractAccountId).hasToString(evmAddress.toString());
            assertThat(info.key).isEqualTo(adminKey.getPublicKey());
        }
    }

    @Test
    @DisplayName("Can create account with alias from admin key with receiver sig required")
    void createAccountWithAliasFromAdminKeyWithReceiverSigRequired() throws Exception {
        // Tests the fourth row of this table
        // https://github.com/hashgraph/hedera-improvement-proposal/blob/d39f740021d7da592524cffeaf1d749803798e9a/HIP/hip-583.md#signatures
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateECDSA();
            var evmAddress = adminKey.getPublicKey().toEvmAddress();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var accountId = new AccountCreateTransaction()
                    .setReceiverSignatureRequired(true)
                    .setKey(adminKey)
                    .setAlias(evmAddress)
                    .freezeWith(testEnv.client)
                    .sign(adminKey)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .accountId;

            assertThat(accountId).isNotNull();

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

            assertThat(info.accountId).isNotNull();
            assertThat(info.contractAccountId).hasToString(evmAddress.toString());
            assertThat(info.key).isEqualTo(adminKey.getPublicKey());
        }
    }

    @Test
    @DisplayName("Cannot create account with alias from admin key with receiver sig required without signature")
    void cannotCreateAccountWithAliasFromAdminKeyWithReceiverSigRequiredAndNoSignature() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateECDSA();
            var evmAddress = adminKey.getPublicKey().toEvmAddress();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            assertThatExceptionOfType(ReceiptStatusException.class)
                    .isThrownBy(() -> new AccountCreateTransaction()
                            .setReceiverSignatureRequired(true)
                            .setKey(adminKey)
                            .setAlias(evmAddress)
                            .freezeWith(testEnv.client)
                            .execute(testEnv.client)
                            .getReceipt(testEnv.client))
                    .withMessageContaining(Status.INVALID_SIGNATURE.toString());
        }
    }

    @Test
    @DisplayName("Can create account with alias different from admin key")
    void createAccountWithAlias() throws Exception {
        // Tests the fifth row of this table
        // https://github.com/hashgraph/hedera-improvement-proposal/blob/d39f740021d7da592524cffeaf1d749803798e9a/HIP/hip-583.md#signatures
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateED25519();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var key = PrivateKey.generateECDSA();
            var evmAddress = key.getPublicKey().toEvmAddress();

            var accountId = new AccountCreateTransaction()
                    .setKey(adminKey)
                    .setAlias(evmAddress)
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .accountId;

            assertThat(accountId).isNotNull();

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

            assertThat(info.accountId).isNotNull();
            assertThat(info.contractAccountId).hasToString(evmAddress.toString());
            assertThat(info.key).isEqualTo(adminKey.getPublicKey());
        }
    }

    @Test
    @DisplayName("Cannot create account with alias different from admin key without signature")
    void cannotCreateAccountWithAliasWithoutSignature() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateED25519();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var key = PrivateKey.generateECDSA();
            var evmAddress = key.getPublicKey().toEvmAddress();

            assertThatExceptionOfType(ReceiptStatusException.class)
                    .isThrownBy(() -> new AccountCreateTransaction()
                            .setReceiverSignatureRequired(true)
                            .setKey(adminKey)
                            .setAlias(evmAddress)
                            .freezeWith(testEnv.client)
                            .execute(testEnv.client)
                            .getReceipt(testEnv.client))
                    .withMessageContaining(Status.INVALID_SIGNATURE.toString());
        }
    }

    @Test
    @DisplayName("Can create account with alias different from admin key with receiver sig required")
    void createAccountWithAliasWithReceiverSigRequired() throws Exception {
        // Tests the sixth row of this table
        // https://github.com/hashgraph/hedera-improvement-proposal/blob/d39f740021d7da592524cffeaf1d749803798e9a/HIP/hip-583.md#signatures
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateED25519();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var key = PrivateKey.generateECDSA();
            var evmAddress = key.getPublicKey().toEvmAddress();

            var accountId = new AccountCreateTransaction()
                    .setReceiverSignatureRequired(true)
                    .setKey(adminKey)
                    .setAlias(evmAddress)
                    .freezeWith(testEnv.client)
                    .sign(key)
                    .sign(adminKey)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .accountId;

            assertThat(accountId).isNotNull();

            var info = new AccountInfoQuery().setAccountId(accountId).execute(testEnv.client);

            assertThat(info.accountId).isNotNull();
            assertThat(info.contractAccountId).hasToString(evmAddress.toString());
            assertThat(info.key).isEqualTo(adminKey.getPublicKey());
        }
    }

    @Test
    @DisplayName(
            "Cannot create account with alias different from admin key and receiver sig required without signature")
    void cannotCreateAccountWithAliasWithReceiverSigRequiredWithoutSignature() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            var adminKey = PrivateKey.generateED25519();

            // Create the admin account
            new AccountCreateTransaction()
                    .setKey(adminKey)
                    .freezeWith(testEnv.client)
                    .execute(testEnv.client);

            var key = PrivateKey.generateECDSA();
            var evmAddress = key.getPublicKey().toEvmAddress();

            assertThatExceptionOfType(ReceiptStatusException.class)
                    .isThrownBy(() -> new AccountCreateTransaction()
                            .setReceiverSignatureRequired(true)
                            .setKey(adminKey)
                            .setAlias(evmAddress)
                            .freezeWith(testEnv.client)
                            .sign(key)
                            .execute(testEnv.client)
                            .getReceipt(testEnv.client))
                    .withMessageContaining(Status.INVALID_SIGNATURE.toString());
        }
    }
}
