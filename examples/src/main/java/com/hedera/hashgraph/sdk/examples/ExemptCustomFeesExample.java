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

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.logger.LogLevel;
import com.hedera.hashgraph.sdk.logger.Logger;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HIP-573: Blanket exemptions for custom fee collectors.
 */
class ExemptCustomFeesExample {

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
        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        var operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Create accounts A, B, and C.
         */
        PrivateKey firstAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey firstAccountPublicKey = firstAccountPrivateKey.getPublicKey();
        AccountId firstAccountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(firstAccountPublicKey)
            .freezeWith(client)
            .sign(firstAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey secondAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey secondAccountPublicKey = secondAccountPrivateKey.getPublicKey();
        AccountId secondAccountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(secondAccountPublicKey)
            .freezeWith(client)
            .sign(secondAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey thirdAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey thirdAccountPublicKey = thirdAccountPrivateKey.getPublicKey();
        AccountId thirdAccountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(thirdAccountPublicKey)
            .freezeWith(client)
            .sign(thirdAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        /*
         * Step 2:
         * Create a fungible token that has three fractional fees:
         * - fee #1 sends 1/100 of the transferred value to collector 0.0.A.;
         * - fee #2 sends 2/100 of the transferred value to collector 0.0.B.;
         * - fee #3 sends 3/100 of the transferred value to collector 0.0.C.
         */
        CustomFractionalFee fee1 = new CustomFractionalFee()
            .setFeeCollectorAccountId(firstAccountId)
            .setNumerator(1)
            .setDenominator(100)
            .setAllCollectorsAreExempt(true);

        CustomFractionalFee fee2 = new CustomFractionalFee()
            .setFeeCollectorAccountId(secondAccountId)
            .setNumerator(2)
            .setDenominator(100)
            .setAllCollectorsAreExempt(true);

        CustomFractionalFee fee3 = new CustomFractionalFee()
            .setFeeCollectorAccountId(thirdAccountId)
            .setNumerator(3)
            .setDenominator(100)
            .setAllCollectorsAreExempt(true);

        TokenCreateTransaction tokenCreateTransaction = new TokenCreateTransaction()
            .setTokenName("HIP-573 Token")
            .setTokenSymbol("H573")
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAutoRenewAccountId(OPERATOR_ID)
            .setAdminKey(operatorPublicKey)
            .setFreezeKey(operatorPublicKey)
            .setWipeKey(operatorPublicKey)
            .setInitialSupply(100_000_000)
            .setDecimals(2)
            .setCustomFees(List.of(fee1, fee2, fee3))
            .freezeWith(client)
            .sign(firstAccountPrivateKey)
            .sign(secondAccountPrivateKey)
            .sign(thirdAccountPrivateKey);

        TokenId tokenId = tokenCreateTransaction
            .execute(client)
            .getReceipt(client)
            .tokenId;
        System.out.println("TokenId: " + tokenId);

        /*
         * Step 3:
         * Collector 0.0.B sends 10_000 units of the token to 0.0.A.
         */
        // Send 10_000 units from the operator to the second account
        new TransferTransaction()
            .addTokenTransfer(tokenId, OPERATOR_ID, -10_000)
            .addTokenTransfer(tokenId, secondAccountId, 10_000)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client);

        TransactionResponse tokenTransferResponse = new TransferTransaction()
            .addTokenTransfer(tokenId, secondAccountId, -10_000)
            .addTokenTransfer(tokenId, firstAccountId, 10_000)
            .freezeWith(client)
            .sign(secondAccountPrivateKey)
            .execute(client);

        /*
         * Step 4:
         * Get the transaction fee for that transfer transaction.
         */
        Hbar transactionFee = tokenTransferResponse
            .getRecord(client)
            .transactionFee;
        System.out.println("Txfee: " + transactionFee);

        /*
         * Step 5:
         * Show that the fee collector accounts in the custom fee list
         * of the token that was created was not charged a custom fee in the transfer.
         */
        Long firstAccountBalanceAfter = new AccountBalanceQuery()
            .setAccountId(firstAccountId)
            .execute(client)
            .tokens.get(tokenId);

        Long secondAccountBalanceAfter = new AccountBalanceQuery()
            .setAccountId(secondAccountId)
            .execute(client)
            .tokens.get(tokenId);

        Long thirdAccountBalanceAfter = new AccountBalanceQuery()
            .setAccountId(thirdAccountId)
            .execute(client)
            .tokens.get(tokenId);

        System.out.println("First account balance after TransferTransaction: " + firstAccountBalanceAfter);
        System.out.println("Second account balance after TransferTransaction: " + secondAccountBalanceAfter);
        System.out.println("Third account balance after TransferTransaction: " + thirdAccountBalanceAfter);

        /*
         * Clean up:
         * Delete created accounts and token.
         */
        Map<TokenId, Long> firstAccountTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(firstAccountId)
            .execute(client)
            .tokens;
        System.out.println("First account token balance (before wipe): " + firstAccountTokensBeforeWipe.get(tokenId));

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(firstAccountTokensBeforeWipe.get(tokenId))
            .setAccountId(firstAccountId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(firstAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(firstAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(secondAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(secondAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(thirdAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(thirdAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
