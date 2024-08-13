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
import java.util.Objects;

class ZeroTokenOperationsExample {

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

        client.setDefaultMaxTransactionFee(new Hbar(10));

        /*
         * Step 1:
         * Generate an ED25519 key pair for an account.
         */
        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create a new account for the contract to interact with in some of its steps.
         */
        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKey(alicePublicKey)
            .setInitialBalance(Hbar.from(10))
            .freezeWith(client);

        transaction = transaction.signWithOperator(client);

        TransactionResponse response = transaction.execute(client);
        AccountId aliceAccountId = response.getReceipt(client).accountId;

        /*
         * Step 3:
         * Instantiate `ContractHelper`.
         */
        ContractHelper contractHelper = new ContractHelper(
            "contracts/precompile/ZeroTokenOperations.json",
            new ContractFunctionParameters()
                .addAddress(OPERATOR_ID.toSolidityAddress())
                .addAddress(aliceAccountId.toSolidityAddress()),
            client
        );

        /*
         * Step 4:
         * Configure steps in `ContractHelper`.
         */
        contractHelper
            .setPayableAmountForStep(0, Hbar.from(20))
            .addSignerForStep(1, alicePrivateKey);

         /*
         * Step 5:
         * Execute steps in `ContractHelper`.
         * - step 0 creates a fungible token;
         * - step 1 Associate with account;
         * - step 2 transfer the token by passing a zero value;
         * - step 3 mint the token by passing a zero value;
         * - step 4 burn the token by passing a zero value;
         * - step 5 wipe the token by passing a zero value.
        */
        contractHelper.executeSteps(/* from step */ 0, /* to step */ 5, client);

        /*
         * Step 6:
         * Create and execute a transfer transaction with a zero value.
         */
        // Create a Fungible Token.
        System.out.println("Attempting to execute step 6");

        TokenCreateTransaction tokenCreateTransaction = new TokenCreateTransaction()
            .setTokenName("Black Sea LimeChain Token")
            .setTokenSymbol("BSL")
            .setTreasuryAccountId(OPERATOR_ID)
            // Total supply = 10000 / 10 ^ 2.
            .setInitialSupply(10_000)
            .setDecimals(2)
            .setAutoRenewAccountId(OPERATOR_ID)
            .freezeWith(client);

        tokenCreateTransaction = tokenCreateTransaction.signWithOperator(client);
        TransactionResponse responseTokenCreate = tokenCreateTransaction.execute(client);

        TokenId tokenId = responseTokenCreate.getReceipt(client).tokenId;

        // Associate Token with Account.
        // Accounts on hedera have to opt in to receive any types of token that aren't Hbar.
        TokenAssociateTransaction tokenAssociateTransaction = new TokenAssociateTransaction()
            .setAccountId(aliceAccountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(client);

        TokenAssociateTransaction signedTxForAssociateToken = tokenAssociateTransaction.sign(alicePrivateKey);
        TransactionResponse txResponseAssociatedToken = signedTxForAssociateToken.execute(client);

        txResponseAssociatedToken.getReceipt(client);

        //Transfer token.
        TransferTransaction transferToken = new TransferTransaction()
            // Deduct 0 tokens.
            .addTokenTransfer(tokenId, OPERATOR_ID, 0)
            // Increase balance by 0.
            .addTokenTransfer(tokenId, aliceAccountId, 0)
            .freezeWith(client);

        TransferTransaction signedTransferTokenTX = transferToken.signWithOperator(client);
        TransactionResponse txResponseTransferToken = signedTransferTokenTX.execute(client);

        // Verify the transaction reached consensus.
        TransactionRecord transferReceiptRecord = txResponseTransferToken.getRecord(client);

        System.out.println(
            "step 6 completed, and returned valid result. TransactionId: " + transferReceiptRecord.transactionId);

        System.out.println("All steps completed with valid results.");

        /*
         * Clean up:
         * Delete created account and contract.
         */
        new AccountDeleteTransaction()
            .setAccountId(aliceAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(alicePrivateKey)
            .execute(client)
            .getReceipt(client);

        new ContractDeleteTransaction()
            .setContractId(contractHelper.contractId)
            .setTransferAccountId(OPERATOR_ID)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
