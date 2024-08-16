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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * How to set and receive custom fees.
 */
class CustomFeesExample {

    /*
     * See .env.sample in the examples folder root for how to specify values below
     * or set environment variables with the same names.
     */

    /**
     * Operator's account ID.
     * Used to sign and pay for operations on Hedera.
     */
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    /**
     * Operator's private key.
     */
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

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
        System.out.println("Custom Fees Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Create three accounts: Alice, Bob, and Charlie.
         *
         * Alice will be the treasury for our example token.
         * Fees only apply in transactions not involving the treasury, so we need two other accounts.
         */
        System.out.println("Creating Alice's, Bob's and Charlie's accounts...");

        Hbar initialBalance = Hbar.from(1);
        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();
        AccountId aliceId = new AccountCreateTransaction()
            .setInitialBalance(initialBalance)
            .setKey(alicePublicKey)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey bobPrivateKey = PrivateKey.generateED25519();
        PublicKey bobPublicKey = bobPrivateKey.getPublicKey();
        AccountId bobId = new AccountCreateTransaction()
            .setInitialBalance(initialBalance)
            .setKey(bobPublicKey)
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        PrivateKey charliePrivateKey = PrivateKey.generateED25519();
        PublicKey charliePublicKey = charliePrivateKey.getPublicKey();
        AccountId charlieId = new AccountCreateTransaction()
            .setInitialBalance(initialBalance)
            .setKey(charliePublicKey)
            .freezeWith(client)
            .sign(charliePrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        System.out.println("Alice's account ID: " + aliceId);
        System.out.println("Bob's account ID: " + bobId);
        System.out.println("Charlie's account ID: " + charlieId);

        /*
         * Step 2:
         * Create a custom fee list of 1 fixed fee.
         *
         * A custom fee list can be a list of up to 10 custom fees,
         * where each fee is a fixed fee or a fractional fee.
         * This fixed fee will mean that every time Bob transfers any number of tokens to Charlie,
         * Alice will collect 1 Hbar from each account involved in the transaction who is SENDING
         * the Token (in this case, Bob).
         *
         * In this example the fee is in Hbar, but you can charge a fixed fee in a token if you'd like.
         * E.g., you can make it so that each time an account transfers Foo tokens,
         * they must pay a fee in Bar tokens to the fee collecting account.
         * To charge a fixed fee in tokens, instead of calling setHbarAmount(), call
         * setDenominatingTokenId(tokenForFee) and setAmount(tokenFeeAmount).
         */
        CustomFixedFee customHbarFee = new CustomFixedFee()
            .setHbarAmount(Hbar.from(1))
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> hbarFeeList = Collections.singletonList(customHbarFee);

        /*
         * Step 3:
         * Create a fungible token.
         *
         * Setting the feeScheduleKey to Alice's key will enable Alice to change the custom
         * fees list on this token later using the TokenFeeScheduleUpdateTransaction.
         * We will create an initial supply of 100 of these tokens.
         */
        System.out.println("Creating new Fungible Token using the Hedera Token Service...");

        TokenId tokenId = new TokenCreateTransaction()
            .setTokenName("Example Fungible Token")
            .setTokenSymbol("EFT")
            .setAdminKey(alicePublicKey)
            .setSupplyKey(alicePublicKey)
            .setFeeScheduleKey(alicePublicKey)
            .setWipeKey(alicePublicKey)
            .setTreasuryAccountId(aliceId)
            .setCustomFees(hbarFeeList)
            .setInitialSupply(100)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        TokenInfo tokenInfo1 = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Created new fungible token with ID: " + tokenId + " and custom fees: " + tokenInfo1.customFees);

        /*
         * Step 4:
         * Associate the token with Bob and Charlie before they can transfer and receive it.
         */
        System.out.println("Associate created fungible token with Bob's and Charlie's accounts...");

        new TokenAssociateTransaction()
            .setAccountId(bobId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getReceipt(client);

        new TokenAssociateTransaction()
            .setAccountId(charlieId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(charliePrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 5:
         * Transfer all 100 tokens to Bob.
         */
        System.out.println("Transferring all 100 tokens from Alice to Bob...");

        new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, 100)
            .addTokenTransfer(tokenId, aliceId, -100)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 6:
         * Check Alice's Hbar balance.
         */
        Hbar aliceHbar1 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .hbars;

        if (aliceHbar1.equals(initialBalance)) {
            System.out.println("Alice's Hbar balance before: " + aliceHbar1);
        } else {
            throw new Exception("Alice's account initial balance was not set correctly! (Fail)");
        }

        /*
         * Step 7:
         * Transfer 20 tokens from Bob to Charlie.
         */
        System.out.println("Transferring 20 tokens from Bob to Charlie...");

        TransactionRecord record1 = new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, -20)
            .addTokenTransfer(tokenId, charlieId, 20)
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getRecord(client);

        /*
         * Step 8:
         * Check Alice's Hbar balance.
         *
         * It should increase, because of the fee taken from the transfer in the previous step.
         */
        Hbar aliceHbar2 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .hbars;

        if (aliceHbar2.equals(Hbar.from(2))) {
            System.out.println("Alice's Hbar balance after Bob transferred 20 tokens to Charlie: " + aliceHbar2);
        } else {
            throw new Exception("Custom fee was not set correctly! (Fail)");
        }

        System.out.println("Assessed fees: " + record1.assessedCustomFees);

        /*
         * Step 9:
         * Use the TokenUpdateFeeScheduleTransaction with Alice's key to change the custom fees on our token.
         *
         * TokenUpdateFeeScheduleTransaction will replace the list of fees that apply to the token with
         * an entirely new list. Let's charge a 10% fractional fee. This means that when Bob attempts to transfer
         * 20 tokens to Charlie, 10% of the tokens he attempts to transfer (2 in this case) will be transferred to
         * Alice instead.
         *
         * Fractional fees default to FeeAssessmentMethod.INCLUSIVE, which is the behavior described above.
         * If you set the assessment method to EXCLUSIVE, then when Bob attempts to transfer 20 tokens to Charlie,
         * Charlie will receive all 20 tokens, and Bob will be charged an additional 10% fee which
         * will be transferred to Alice.
         */
        CustomFractionalFee customFractionalFee = new CustomFractionalFee()
            .setNumerator(1)
            .setDenominator(10)
            .setMin(1)
            .setMax(10)
            // .setAssessmentMethod(FeeAssessmentMethod.EXCLUSIVE)
            .setFeeCollectorAccountId(aliceId);
        List<CustomFee> fractionalFeeList = Collections.singletonList(customFractionalFee);

        System.out.println("Updating the custom fees for a fungible token...");

        new TokenFeeScheduleUpdateTransaction()
            .setTokenId(tokenId)
            .setCustomFees(fractionalFeeList)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        TokenInfo tokenInfo2 = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Updated custom fees: " + tokenInfo2.customFees);

        /*
         * Step 10:
         * Check Alice's token balance.
         */
        Map<TokenId, Long> aliceTokens3 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;
        if (aliceTokens3.get(tokenId) == 0) {
            System.out.println("Alice's token balance before Bob transfers 20 tokens to Charlie: " + aliceTokens3.get(tokenId));
        } else {
            throw new Exception("Alice's account initial token balance is not zero! (Fail)");
        }

        /*
         * Step 11:
         * Transfer 20 tokens from Bob to Charlie.
         */
        System.out.println("Transferring 20 tokens from Bob to Charlie...");

        TransactionRecord record2 = new TransferTransaction()
            .addTokenTransfer(tokenId, bobId, -20)
            .addTokenTransfer(tokenId, charlieId, 20)
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getRecord(client);

        /*
         * Step 12:
         * Check Alice's token balance. It should increase, because of the fee taken from the
         * transfer in the previous step.
         */
        Map<TokenId, Long> aliceTokens4 = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;

        if (aliceTokens4.get(tokenId) == 2) {
            System.out.println("Alice's token balance after Bob transfers 20 tokens to Charlie: " + aliceTokens4.get(tokenId));
        } else {
            throw new Exception("Custom fractional fee was not set correctly! (Fail)");
        }

        System.out.println("Token transfers: " + record2.tokenTransfers);
        System.out.println("Assessed fees: " + record2.assessedCustomFees);

        /*
         * Clean up:
         * Delete created accounts and tokens.
         */

        // Move token to operator account.
        new TokenAssociateTransaction()
            .setAccountId(client.getOperatorAccountId())
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setAdminKey(OPERATOR_KEY)
            .setSupplyKey(OPERATOR_KEY)
            .setFeeScheduleKey(OPERATOR_KEY)
            .setWipeKey(OPERATOR_KEY)
            .setTreasuryAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        // Wipe token on created accounts.
        Map<TokenId, Long> charlieTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(charlieId)
            .execute(client)
            .tokens;

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(charlieTokensBeforeWipe.get(tokenId))
            .setAccountId(charlieId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        Map<TokenId, Long> bobTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(bobId)
            .execute(client)
            .tokens;

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(bobTokensBeforeWipe.get(tokenId))
            .setAccountId(bobId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        Map<TokenId, Long> aliceTokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(aliceId)
            .execute(client)
            .tokens;

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(aliceTokensBeforeWipe.get(tokenId))
            .setAccountId(aliceId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        // Delete created accounts.
        new AccountDeleteTransaction()
            .setAccountId(charlieId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(charliePrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(bobId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(bobPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(aliceId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        // Delete created token.
        new TokenDeleteTransaction()
            .setTokenId(tokenId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Custom Fees Example Complete!");
    }
}
