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
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Arrays;
import java.util.Objects;

/*
This example just instantiates the solidity contract
defined in examples/src/main/resources/precompile-example/PrecompileExample.sol, which has been
compiled into examples/src/main/resources/precompile-example/PrecompileExample.json.

You should go look at that PrecompileExample.sol file, because that's where the meat of this example is.

This example uses the ContractHelper class (defined in ./ContractHelper.java) to declutter things.

When this example spits out a raw response code,
you can look it up here: https://github.com/hashgraph/hedera-protobufs/blob/main/services/response_code.proto
 */

public class SolidityPrecompileExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private SolidityPrecompileExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // We need a new account for the contract to interact with in some of its steps

        PrivateKey alicePrivateKey = PrivateKey.generateED25519();
        PublicKey alicePublicKey = alicePrivateKey.getPublicKey();
        AccountId aliceAccountId = Objects.requireNonNull(new AccountCreateTransaction()
            .setKey(alicePublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client)
            .getReceipt(client)
            .accountId
        );

        // Instantiate ContractHelper

        ContractHelper contractHelper = new ContractHelper(
            "precompile-example/PrecompileExample.json",
            new ContractFunctionParameters()
                .addAddress(OPERATOR_ID.toSolidityAddress())
                .addAddress(aliceAccountId.toSolidityAddress()),
            client
        );

        // Configure steps in ContracHelper

        contractHelper
            .setResultValidatorForStep(0, contractFunctionResult -> {
                System.out.println("getPseudoRandomSeed() returned " + Arrays.toString(contractFunctionResult.getBytes32(0)));
                return true;
            }).setPayableAmountForStep(1, Hbar.from(20))
            // step 3 associates Alice with the token, which requires Alice's signature
            .addSignerForStep(3, alicePrivateKey)
            .addSignerForStep(5, alicePrivateKey)
            .setParameterSupplierForStep(11, () -> {
                return new ContractFunctionParameters()
                    // when contracts work with a public key, they handle the raw bytes of the public key
                    .addBytes(alicePublicKey.toBytesRaw());
            }).setPayableAmountForStep(11, Hbar.from(40))
            // Because we're setting the adminKey for the created NFT token to Alice's key,
            // Alice must sign the ContractExecuteTransaction.
            .addSignerForStep(11, alicePrivateKey)
            // and Alice must sign for minting because her key is the supply key.
            .addSignerForStep(12, alicePrivateKey)
            .setParameterSupplierForStep(12, () -> {
                return new ContractFunctionParameters()
                    // add three metadatas
                    .addBytesArray(new byte[][]{new byte[]{0x01b}, new byte[]{0x02b}, new byte[]{0x03b}});
            }) // and alice must sign to become associated with the token.
            .addSignerForStep(13, alicePrivateKey)
            // Alice must sign to burn the token because her key is the supply key
            .addSignerForStep(16, alicePrivateKey);

        // step 0 tests pseudo random number generator (PRNG)
        // step 1 creates a fungible token
        // step 2 mints it
        // step 3 associates Alice with it
        // step 4 transfers it to Alice.
        // step 5 approves an allowance of the fungible token with operator as the owner and Alice as the spender [NOT WORKING]
        // steps 6 - 10 test misc functions on the fungible token (see PrecompileExample.sol for details).
        // step 11 creates an NFT token with a custom fee, and with the admin and supply set to Alice's key
        // step 12 mints some NFTs
        // step 13 associates Alice with the NFT token
        // step 14 transfers some NFTs to Alice
        // step 15 approves an NFT allowance with operator as the owner and Alice as the spender [NOT WORKING]
        // step 16 burn some NFTs

        contractHelper.executeSteps(/* from step */ 0, /* to step */ 16, client);

        System.out.println("All steps completed with valid results.");

        // Clean up

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
    }
}
