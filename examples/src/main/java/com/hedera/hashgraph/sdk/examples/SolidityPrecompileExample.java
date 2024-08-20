/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2022 - 2024 Hedera Hashgraph, LLC
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

import java.util.Arrays;
import java.util.Objects;

/**
 * This example just instantiates the solidity contract
 * defined in `resources/com/hedera/hashgraph/sdk/examples/contracts/precompile/PrecompileExample.sol`, which has been
 * compiled into `resources/com/hedera/hashgraph/sdk/examples/contracts/precompile/PrecompileExample.json`.
 * <p>
 * You should go look at that `PrecompileExample.sol` file, because that's where the meat of this example is.
 * <p>
 * This example uses the ContractHelper class (defined in ./ContractHelper.java) to declutter things.
 * <p>
 * When this example spits out a raw response code,
 * you can look it up here: https://github.com/hashgraph/hedera-protobufs/blob/main/services/response_code.proto
 */
// TODO: update description
class SolidityPrecompileExample {

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
        System.out.println("Solidity Precompile Example Start!");

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
         * Generate ED25519 key pair.
         */
        System.out.println("Generating ED25519 key pair...");
        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create a new account for the contract to interact with in some of its steps.
         */
        System.out.println("Creating Alice account...");
        AccountId aliceAccountId = Objects.requireNonNull(new AccountCreateTransaction()
            .setKey(alicePublicKey)
            .setInitialBalance(Hbar.from(1))
            .execute(client)
            .getReceipt(client)
            .accountId
        );
        Objects.requireNonNull(aliceAccountId);
        System.out.println("Created Alice's account with ID: " + aliceAccountId);

        /*
         * Step 3:
         * Instantiate ContractHelper.
         */
        System.out.println("Instantiating `ContractHelper`...");
        ContractHelper contractHelper = new ContractHelper(
            "contracts/precompile/PrecompileExample.json",
            new ContractFunctionParameters()
                .addAddress(OPERATOR_ID.toSolidityAddress())
                .addAddress(aliceAccountId.toSolidityAddress()),
            client
        );

        /*
         * Step 4:
         * Configure steps in ContractHelper.
         */
        System.out.println("Configuring steps in `ContractHelper`...");
        contractHelper
            .setResultValidatorForStep(0, contractFunctionResult -> {
                System.out.println("getPseudoRandomSeed() returned " + Arrays.toString(contractFunctionResult.getBytes32(0)));
                return true;
            }).setPayableAmountForStep(1, Hbar.from(20)) // TODO: double check when example is fixed
            // Step 3 associates Alice with the token, which requires Alice's signature.
            .addSignerForStep(3, alicePrivateKey)
            .addSignerForStep(5, alicePrivateKey)
            .setParameterSupplierForStep(11, () -> new ContractFunctionParameters()
                // When contracts work with a public key, they handle the raw bytes of the public key.
                .addBytes(alicePublicKey.toBytesRaw())).setPayableAmountForStep(11, Hbar.from(40))
            // Because we're setting the adminKey for the created NFT token to Alice's key,
            // Alice must sign the ContractExecuteTransaction.
            .addSignerForStep(11, alicePrivateKey)
            // And Alice must sign for minting because her key is the supply key.
            .addSignerForStep(12, alicePrivateKey)
            .setParameterSupplierForStep(12, () -> new ContractFunctionParameters()
                // Add three metadatas. Alice must sign to become associated with the token.
                .addBytesArray(new byte[][]{new byte[]{0x01b}, new byte[]{0x02b}, new byte[]{0x03b}}))
            .addSignerForStep(13, alicePrivateKey)
            // Alice must sign to burn the token because her key is the supply key.
            .addSignerForStep(16, alicePrivateKey);


        /*
         * Step 5:
         * Execute steps in `ContractHelper`.
         * - step 0 tests pseudo random number generator (PRNG);
         * - step 1 creates a fungible token;
         * - step 2 mints it;
         * - step 3 associates Alice with it;
         * - step 4 transfers it to Alice;
         * - step 5 approves an allowance of the fungible token with operator as the owner and Alice as the spender;
         * - steps 6 - 10 test misc functions on the fungible token (see PrecompileExample.sol for details);
         * - step 11 creates an NFT token with a custom fee, and with the admin and supply set to Alice's key;
         * - step 12 mints some NFTs;
         * - step 13 associates Alice with the NFT token;
         * - step 14 transfers some NFTs to Alice;
         * - step 15 approves an NFT allowance with operator as the owner and Alice as the spender;
         * - step 16 burn some NFTs.
         */
        System.out.println("Executing steps in `ContractHelper`.");
        contractHelper.executeSteps(/* from step */ 0, /* to step */ 16, client);

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

        System.out.println("Solidity Precompile Example Complete!");
    }
}
