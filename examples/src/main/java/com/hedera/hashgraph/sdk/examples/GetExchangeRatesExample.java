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
package com.hedera.hashgraph.sdk.examples;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * How to get exchange rates info from the Hedera network.
 */
class GetExchangeRatesExample {

    // See `.env.sample` in the `examples` folder root for how to specify values below
    // or set environment variables with the same names.

    // Operator's account ID.
    // Used to sign and pay for operations on Hedera.
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    // Operator's private key.
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // `HEDERA_NETWORK` defaults to `testnet` if not specified in dotenv file
    // Networks can be: `localhost`, `testnet`, `previewnet`, `mainnet`.
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    // `SDK_LOG_LEVEL` defaults to `SILENT` if not specified in dotenv file
    // Log levels can be: `TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`, `SILENT`.
    // Important pre-requisite: set simple logger log level to same level as the SDK_LOG_LEVEL,
    // for example via VM options: `-Dorg.slf4j.simpleLogger.log.com.hedera.hashgraph=trace`
    private static final String SDK_LOG_LEVEL = Dotenv.load().get("SDK_LOG_LEVEL", "SILENT");

    public static void main(String[] args) throws Exception {
        System.out.println("Get Exchange Rates Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Get contents of the file `0.0.112`. It is a system file, where exchange rate is stored.
         */
        System.out.println("Getting contents of the file `0.0.112`...");
        ByteString fileContentsByteString = new FileContentsQuery()
            .setFileId(FileId.fromString("0.0.112"))
            .execute(client);

        /*
         * Step 2:
         * Parse file contents to an `ExchangeRates` object.
         */
        byte[] fileContents = fileContentsByteString.toByteArray();
        ExchangeRates exchangeRateSet = ExchangeRates.fromBytes(fileContents);

        /*
         * Step 3:
         * Print the info.
         */
        System.out.println("Current numerator: " + exchangeRateSet.currentRate.cents);
        System.out.println("Current denominator: " + exchangeRateSet.currentRate.hbars);
        System.out.println("Current expiration time: " + exchangeRateSet.currentRate.expirationTime.toString());
        System.out.println("Current Exchange Rate: " + exchangeRateSet.currentRate.exchangeRateInCents);
        System.out.println("Next numerator: " + exchangeRateSet.nextRate.cents);
        System.out.println("Next denominator: " + exchangeRateSet.nextRate.hbars);
        System.out.println("Next expiration time: " + exchangeRateSet.nextRate.expirationTime.toString());
        System.out.println("Next Exchange Rate: " + exchangeRateSet.nextRate.exchangeRateInCents);

        /*
         * Clean up:
         */
        client.close();
        System.out.println("Get Exchange Rates Example Complete!");
    }
}
