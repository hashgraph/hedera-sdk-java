// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.examples;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;
import org.hiero.sdk.AccountBalanceQuery;
import org.hiero.sdk.AccountId;
import org.hiero.sdk.Client;
import org.hiero.sdk.Hbar;
import org.hiero.sdk.logger.LogLevel;
import org.hiero.sdk.logger.Logger;

/**
 * How to get balance of a Hedera account.
 */
class GetAccountBalanceExample {

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
     * HEDERA_NETWORK defaults to testnet if not specified in dotenv file.
     * Network can be: localhost, testnet, previewnet or mainnet.
     */
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    /**
     * SDK_LOG_LEVEL defaults to SILENT if not specified in dotenv file.
     * Log levels can be: TRACE, DEBUG, INFO, WARN, ERROR, SILENT.
     * <p>
     * Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
     * for example via VM options: -Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace
     */
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Get Account Balance Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         *
         * Because AccountBalanceQuery is a free query, we can make it without setting an operator on the client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Execute AccountBalanceQuery and output operator's account balance.
         */
        Hbar operatorsBalance =
                new AccountBalanceQuery().setAccountId(OPERATOR_ID).execute(client).hbars;

        System.out.println("Operator's Hbar account balance: " + operatorsBalance);

        /*
         * Clean up:
         */
        client.close();
        System.out.println("Get Account Balance Example Complete!");
    }
}
