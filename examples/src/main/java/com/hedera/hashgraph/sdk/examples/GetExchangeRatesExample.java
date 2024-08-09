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
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;

/**
 * How to get exchange rates info from the Hedera network.
 */
class GetExchangeRatesExample {

    // See `.env.sample` in the `examples` folder root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args) throws Exception {
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1:
         * Get contents of file `0.0.112`. It is a system file, where exchange rate is stored.
         */
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
        System.out.println("Example complete!");
    }
}
