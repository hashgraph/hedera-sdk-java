// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.examples;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;
import org.bouncycastle.util.encoders.Hex;
import org.hiero.sdk.*;
import org.hiero.sdk.logger.LogLevel;
import org.hiero.sdk.logger.Logger;

public class AccountCreateWithAndWithoutAliasExample {
    /*
     * Constants for default network and log level configurations
     */
    private static final String DEFAULT_NETWORK = "testnet";
    private static final String DEFAULT_LOG_LEVEL = "SILENT";

    /*
     * Client and operator credentials for Hedera network interactions
     */
    private final Client client;
    private final AccountId operatorId;
    private final PrivateKey operatorKey;

    /**
     * Constructor to initialize Hedera client with environment configurations
     */
    public AccountCreateWithAndWithoutAliasExample() throws InterruptedException {
        /*
         * Step 0: Load environment configurations
         * - Retrieve OPERATOR_ID and OPERATOR_KEY from .env file
         * - Set default network and log level if not specified
         */
        Dotenv env = Dotenv.load();
        this.operatorId =
                AccountId.fromString(Objects.requireNonNull(env.get("OPERATOR_ID"), "OPERATOR_ID must be set"));
        this.operatorKey =
                PrivateKey.fromString(Objects.requireNonNull(env.get("OPERATOR_KEY"), "OPERATOR_KEY must be set"));
        String network = env.get("HEDERA_NETWORK", DEFAULT_NETWORK);
        String logLevel = env.get("SDK_LOG_LEVEL", DEFAULT_LOG_LEVEL);

        /*
         * Step 1: Initialize Hedera client with network and logging configurations
         */
        this.client = initializeClient(network, logLevel);
    }

    /**
     * Initialize Hedera client with specified network and log level
     */
    private Client initializeClient(String network, String logLevel) throws InterruptedException {
        Client client = ClientHelper.forName(network);
        client.setOperator(operatorId, operatorKey);
        client.setLogger(new Logger(LogLevel.valueOf(logLevel)));
        return client;
    }

    /**
     * Create account with EVM address alias using single key
     */
    private void createAccountWithAlias() throws Exception {
        /*
         * Step 2: Generate ECDSA key pair
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();
        PublicKey publicKey = privateKey.getPublicKey();
        EvmAddress evmAddress = publicKey.toEvmAddress();

        /*
         * Step 3: Create account transaction with key alias
         */
        AccountCreateTransaction transaction =
                new AccountCreateTransaction().setKeyWithAlias(privateKey).freezeWith(client);

        /*
         * Step 4: Sign and execute transaction
         */
        transaction.sign(privateKey);
        TransactionResponse response = transaction.execute(client);

        /*
         * Step 5: Retrieve and verify account information
         */
        AccountId accountId = response.getReceipt(client).accountId;
        AccountInfo info = new AccountInfoQuery().setAccountId(accountId).execute(client);

        System.out.println("Initial EVM address: " + evmAddress + " is the same as " + info.contractAccountId);
    }

    /**
     * Create account with both ED25519 and ECDSA keys
     */
    private void createAccountWithBothKeys() throws Exception {
        /*
         * Step 6: Generate ED25519 and ECDSA key pairs
         */
        PrivateKey ed25519Key = PrivateKey.generateED25519();
        PrivateKey ecdsaKey = PrivateKey.generateECDSA();
        EvmAddress evmAddress = ecdsaKey.getPublicKey().toEvmAddress();

        /*
         * Step 7: Create account transaction with multiple keys
         */
        AccountCreateTransaction transaction = new AccountCreateTransaction()
                .setKeyWithAlias(ed25519Key, ecdsaKey)
                .freezeWith(client);

        /*
         * Step 8: Sign transaction with both keys
         */
        transaction.sign(ed25519Key);
        transaction.sign(ecdsaKey);
        TransactionResponse response = transaction.execute(client);

        /*
         * Step 9: Retrieve and verify account information
         */
        AccountId accountId = response.getReceipt(client).accountId;
        AccountInfo info = new AccountInfoQuery().setAccountId(accountId).execute(client);

        System.out.println("Account's key: " + info.key + " is the same as " + ed25519Key.getPublicKey());
        System.out.println("Initial EVM address: " + evmAddress + " is the same as " + info.contractAccountId);
    }

    /**
     * Create account without EVM address alias
     */
    private void createAccountWithoutAlias() throws Exception {
        /*
         * Step 10: Generate ECDSA key pair
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();

        /*
         * Step 11: Create account transaction without alias
         */
        AccountCreateTransaction transaction =
                new AccountCreateTransaction().setKeyWithoutAlias(privateKey).freezeWith(client);

        /*
         * Step 12: Sign and execute transaction
         */
        transaction.sign(privateKey);
        TransactionResponse response = transaction.execute(client);

        /*
         * Step 13: Retrieve and verify account information
         */
        AccountId accountId = response.getReceipt(client).accountId;
        AccountInfo info = new AccountInfoQuery().setAccountId(accountId).execute(client);

        System.out.println("Account's key: " + info.key + " is the same as " + privateKey.getPublicKey());
        System.out.println("Account has no alias: " + isZeroAddress(Hex.decode(info.contractAccountId)));
    }

    /**
     * Check if the first 12 bytes of an address are zero
     */
    private static boolean isZeroAddress(byte[] address) {
        for (int i = 0; i < 12; i++) {
            if (address[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Main method to demonstrate account creation scenarios
     */
    public static void main(String[] args) throws Exception {
        /*
         * Step 14: Create example instance and demonstrate account creation methods
         */
        AccountCreateWithAndWithoutAliasExample accountCreateWithAndWithoutAliasExample =
                new AccountCreateWithAndWithoutAliasExample();

        accountCreateWithAndWithoutAliasExample.createAccountWithAlias();
        accountCreateWithAndWithoutAliasExample.createAccountWithBothKeys();
        accountCreateWithAndWithoutAliasExample.createAccountWithoutAlias();
    }
}
