// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.examples;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;
import org.hiero.sdk.*;
import org.hiero.sdk.logger.LogLevel;
import org.hiero.sdk.logger.Logger;

/**
 * How to create a Hedera account with alias.
 *
 */
class CreateAccountWithAliasExample {

    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Operator's account ID.
     * Used to sign and pay for operations on Hedera.
     */
    private static final AccountId OPERATOR_ID =
            AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    /**
     * Operator's private key.
     */
    private static final PrivateKey OPERATOR_KEY =
            PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    /**
     * HEDERA_NETWORK defaults to testnet if not specified in dotenv file.
     * Network can be: localhost, testnet, previewnet or mainnet.
     */
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    /**
     * SDK_LOG_LEVEL defaults to SILENT if not specified in dotenv file.
     * Log levels can be: TRACE, DEBUG, INFO, WARN, ERROR, SILENT.
     * <p>
     * Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
     * for example via VM options: -Dorg.slf4j.simpleLogger.log.org.hiero=trace
     */
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Create Account With Alias Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Generate ECSDA private key.
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();

        /*
         * Step 2:
         * Extract ECDSA public key.
         */
        PublicKey publicKey = privateKey.getPublicKey();

        /*
         * Step 3:
         * Extract Ethereum public address.
         */
        EvmAddress evmAddress = publicKey.toEvmAddress();
        System.out.println("EVM address of the new account: " + evmAddress);

        /*
         * Step 4:
         * Create new account.
         *
         * Set the EVM address field to the Ethereum public address.
         */
        AccountCreateTransaction accountCreateTx = new AccountCreateTransaction()
                .setInitialBalance(Hbar.from(1))
                .setKey(operatorPublicKey)
                .setAlias(evmAddress)
                .freezeWith(client);

        /*
         * Step 5:
         * Sign the AccountCreateTransaction transaction using an existing Hedera account and key paying for the transaction fee.
         */
        accountCreateTx.sign(privateKey);
        TransactionResponse accountCreateTxResponse = accountCreateTx.execute(client);

        AccountId newAccountId = new TransactionReceiptQuery()
                .setTransactionId(accountCreateTxResponse.transactionId)
                .execute(client)
                .accountId;
        Objects.requireNonNull(newAccountId);
        System.out.println("Created account with ID: " + newAccountId);

        /*
         * Step 6:
         * Get the AccountInfo and show that the account has contractAccountId.
         */
        AccountInfo newAccountInfo =
                new AccountInfoQuery().setAccountId(newAccountId).execute(client);

        if (newAccountInfo.contractAccountId != null) {
            System.out.println("The newly account has alias: " + newAccountInfo.contractAccountId);
        } else {
            throw new Exception("The newly account doesn't have alias! (Fail)");
        }

        /*
         * Clean up:
         * Delete created account.
         */
        new AccountDeleteTransaction()
                .setAccountId(newAccountId)
                .setTransferAccountId(OPERATOR_ID)
                .execute(client)
                .getReceipt(client);

        client.close();

        System.out.println("Create Account With Alias Example Complete!");
    }
}