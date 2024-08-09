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
import io.github.cdimascio.dotenv.Dotenv;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * HIP-336: Approval and Allowance API for Tokens.
 * Describes the addition of Services APIs to approve and exercise allowances to a third party account.
 * The allowances grant another account the right to transfer hbar, fungible and non-fungible tokens from your account.
 *
 * TODO: double check this example (case with delegating spender)
 */
class NftAddRemoveAllowancesExample {

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

        PublicKey operatorPublicKey = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * The beginning of the first example (with NFT).
         * Create an NFT using the Hedera Token Service.
         */
        System.out.println("Example 1: Approve/delete allowances for single serial numbers.");

        String[] CIDs = {
            "QmNPCiNA3Dsu3K5FxDPMG5Q3fZRwVTg14EXA92uqEeSRXn",
            "QmZ4dgAgt8owvnULxnKxNe8YqpavtVCXmc1Lt2XajFpJs9",
            "QmPzY5GxevjyfMUF5vEAjtyRoigzWp47MiKAtLBduLMC1T",
        };

        TransactionReceipt nftCreateReceipt = new TokenCreateTransaction()
            .setTokenName("NFT Token")
            .setTokenSymbol("NFTT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setDecimals(0)
            .setInitialSupply(0)
            .setMaxSupply(CIDs.length)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(operatorPublicKey)
            .setSupplyKey(operatorPublicKey)
            .setWipeKey(operatorPublicKey)
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        TokenId nftTokenId = nftCreateReceipt.tokenId;
        System.out.println("Created NFT with token id: " + nftTokenId);

        /*
         * Step 2:
         * Mint NFTs.
         */
        List<TransactionReceipt> nftCollection = new ArrayList<>();
        for (int i = 0; i < CIDs.length; i++) {
            nftCollection.add(new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .setMetadata(List.of(CIDs[i].getBytes(StandardCharsets.UTF_8)))
                .freezeWith(client)
                .execute(client)
                .getReceipt(client));

            System.out.println("Created NFT " + nftTokenId + " with serial: " + nftCollection.get(i).serials.get(0));
        }

        /*
         * Step 3:
         * Create spender and receiver accounts.
         */
        PrivateKey spenderPrivateKey = PrivateKey.generateECDSA();
        PublicKey spenderPublicKey = spenderPrivateKey.getPublicKey();
        AccountId spenderAccountId = new AccountCreateTransaction()
            .setKey(spenderPublicKey)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("spenderAccountId: " + spenderAccountId);

        PrivateKey receiverPrivateKey = PrivateKey.generateECDSA();
        PublicKey receiverPublicKey = receiverPrivateKey.getPublicKey();
        AccountId receiverAccountId = new AccountCreateTransaction()
            .setKey(receiverPublicKey)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("receiverAccountId: " + receiverAccountId);

        /*
         * Step 4:
         * Associate spender and receiver accounts with the NFT.
         */
        TransactionReceipt spenderAssociateReceipt = new TokenAssociateTransaction()
            .setAccountId(spenderAccountId)
            .setTokenIds(List.of(nftTokenId))
            .freezeWith(client)
            .sign(spenderPrivateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Spender associate TX status: " + spenderAssociateReceipt.status);

        TransactionReceipt receiverAssociateReceipt = new TokenAssociateTransaction()
            .setAccountId(receiverAccountId)
            .setTokenIds(List.of(nftTokenId))
            .freezeWith(client)
            .sign(receiverPrivateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Receiver associate TX status: " + receiverAssociateReceipt.status);

        /*
         * Step 5:
         * Approve NFT (serial '1' and '2') allowance for spender account.
         */
        NftId nft1 = new NftId(nftTokenId, 1);
        NftId nft2 = new NftId(nftTokenId, 2);

        TransactionReceipt approveReceipt = new AccountAllowanceApproveTransaction()
            .approveTokenNftAllowance(nft1, OPERATOR_ID, spenderAccountId)
            .approveTokenNftAllowance(nft2, OPERATOR_ID, spenderAccountId)
            .execute(client)
            .getReceipt(client);
        System.out.println("Approve spender allowance for serials 1 and 2 - status: " + approveReceipt.status);

        /*
         * Step 6:
         * Send NFT with serial number 1 from operator's to receiver account.
         * This transaction should be executed on behalf of the spender and should end up with `SUCCESS`.
         */
        // Generate TransactionId from spender's account id in order,
        // for the transaction to be executed on behalf of the spender.
        TransactionId onBehalfOfTransactionId = TransactionId.generate(spenderAccountId);

        TransactionReceipt approvedSendReceipt = new TransferTransaction()
            .addApprovedNftTransfer(nft1, OPERATOR_ID, receiverAccountId)
            .setTransactionId(onBehalfOfTransactionId)
            .freezeWith(client)
            .sign(spenderPrivateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Transfer serial 1 on behalf of the spender status:" + approvedSendReceipt.status);

        /*
         * Step 7:
         * Remove NFT (serial '2') allowance for any account.
         */
        TransactionReceipt deleteAllowanceReceipt = new AccountAllowanceDeleteTransaction()
            .deleteAllTokenNftAllowances(nft2, OPERATOR_ID)
            .execute(client)
            .getReceipt(client);
        System.out.println("Remove spender's allowance for serial 2 - status: " + deleteAllowanceReceipt.status);

        /*
         * Step 8:
         * Send NFT with serial number 2 from operator's to receiver account.
         * Spender does not have an allowance to send serial 2, should end up with `SPENDER_DOES_NOT_HAVE_ALLOWANCE`.
         */
        TransactionId onBehalfOfTransactionId2 = TransactionId.generate(spenderAccountId);

        try {
            new TransferTransaction()
                .addApprovedNftTransfer(nft2, OPERATOR_ID, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId2)
                .freezeWith(client)
                .sign(spenderPrivateKey)
                .execute(client)
                .getReceipt(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        /*
         * Step 9:
         * The beginning of the second example (with NFT).
         * Create a fungible HTS token using the Hedera Token Service.
         */
        System.out.println("Example 2: Approve/delete allowances for ALL serial numbers at once.");

        String[] CIDs2 = {
            "QmNPCiNA3Dsu3K5FxDPMG5Q3fZRwVTg14EXA92uqEeSRXn",
            "QmZ4dgAgt8owvnULxnKxNe8YqpavtVCXmc1Lt2XajFpJs9",
            "QmPzY5GxevjyfMUF5vEAjtyRoigzWp47MiKAtLBduLMC1T",
        };

        TransactionReceipt nftCreateReceipt2 = new TokenCreateTransaction()
            .setTokenName("NFT Token")
            .setTokenSymbol("NFTT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setDecimals(0)
            .setInitialSupply(0)
            .setMaxSupply(CIDs2.length)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(operatorPublicKey)
            .setSupplyKey(operatorPublicKey)
            .setWipeKey(operatorPublicKey)
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        TokenId nftTokenId2 = nftCreateReceipt2.tokenId;
        System.out.println("Created NFT with token id: " + nftTokenId2);

        List<TransactionReceipt> nftCollection2 = new ArrayList<>();
        for (int i = 0; i < CIDs2.length; i++) {
            nftCollection2.add(new TokenMintTransaction()
                .setTokenId(nftTokenId2)
                .setMetadata(List.of(CIDs2[i].getBytes(StandardCharsets.UTF_8)))
                .freezeWith(client)
                .execute(client)
                .getReceipt(client));

            System.out.println("Created NFT " + nftTokenId2 + " with serial: " + nftCollection2.get(i).serials.get(0));
        }

        /*
         * Step 10:
         * Create spender and receiver accounts.
         */
        PrivateKey delegatingSpenderPrivateKey = PrivateKey.generateECDSA();
        PublicKey delegatingSpenderPublicKey2 = delegatingSpenderPrivateKey.getPublicKey();
        AccountId delegatingSpenderAccountId = new AccountCreateTransaction()
            .setKey(delegatingSpenderPublicKey2)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("delegatingSpenderAccountId: " + delegatingSpenderAccountId);

        PrivateKey receiverPrivateKey2 = PrivateKey.generateECDSA();
        PublicKey receiverPublicKey2 = receiverPrivateKey2.getPublicKey();
        AccountId receiverAccountId2 = new AccountCreateTransaction()
            .setKey(receiverPublicKey2)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("spenderAccountId: " + receiverAccountId2);

        /*
         * Step 11:
         * Associate spender and receiver accounts with the NFT.
         */
        TransactionReceipt spenderAssociateReceipt2 = new TokenAssociateTransaction()
            .setAccountId(delegatingSpenderAccountId)
            .setTokenIds(List.of(nftTokenId2))
            .freezeWith(client)
            .sign(delegatingSpenderPrivateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Spender associate TX status: " + spenderAssociateReceipt2.status);

        TransactionReceipt receiverAssociateReceipt2 = new TokenAssociateTransaction()
            .setAccountId(receiverAccountId2)
            .setTokenIds(List.of(nftTokenId2))
            .freezeWith(client)
            .sign(receiverPrivateKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Receiver associate TX status: " + receiverAssociateReceipt2.status);

        /*
         * Step 12:
         * Approve NFT (all serials) allowance for spender account.
         */
        NftId example2Nft1 = new NftId(nftTokenId2, 1);
        NftId example2Nft2 = new NftId(nftTokenId2, 2);
        NftId example2Nft3 = new NftId(nftTokenId2, 3);

        TransactionReceipt approveReceipt2 = new AccountAllowanceApproveTransaction()
            .approveTokenNftAllowanceAllSerials(nftTokenId2, OPERATOR_ID, delegatingSpenderAccountId)
            .execute(client)
            .getReceipt(client);
        System.out.println("Approve spender allowance for all serials - status: " + approveReceipt2.status);

        /*
         * Step 13:
         * Create delegate spender account.
         */
        PrivateKey spenderPrivateKey2 = PrivateKey.generateECDSA();
        PublicKey spenderPublicKey2 = spenderPrivateKey2.getPublicKey();
        AccountId spenderAccountId2 = new AccountCreateTransaction()
            .setKey(spenderPublicKey2)
            .setInitialBalance(new Hbar(2))
            .execute(client)
            .getReceipt(client)
            .accountId;
        System.out.println("spenderAccountId2: " + spenderAccountId2);

        /*
         * Step 14:
         * Give `delegatingSpender` allowance for NFT with serial number 3 on behalf of spender account which has `approveForAll` rights.
         */
        TransactionReceipt approveDelegateAllowanceReceipt = new AccountAllowanceApproveTransaction()
            .approveTokenNftAllowance(example2Nft3, OPERATOR_ID, spenderAccountId2, delegatingSpenderAccountId)
            .freezeWith(client)
            .sign(delegatingSpenderPrivateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Approve delegated spender allowance for serial 3 - status: " + approveDelegateAllowanceReceipt.status);

        /*
         * Step 15:
         * Send NFT with serial number 3 from operator's to receiver account.
         * This transaction should be executed on behalf of the `spenderAccountId2`,
         * which has an allowance to send serial 3, and should end up with `SUCCESS`.
         */
        // Generate TransactionId from spender's account id in order,
        // for the transaction to be executed on behalf of the spender.
        TransactionId delegatedOnBehalfOfTxId = TransactionId.generate(spenderAccountId2);

        TransactionReceipt delegatedSendTx = new TransferTransaction()
            .addApprovedNftTransfer(example2Nft3, OPERATOR_ID, receiverAccountId2)
            .setTransactionId(delegatedOnBehalfOfTxId)
            .freezeWith(client)
            .sign(spenderPrivateKey2)
            .execute(client)
            .getReceipt(client);
        System.out.println("Transfer serial 3 on behalf of the delegated spender status:" + delegatedSendTx.status);

        /*
         * Step 16:
         * Send NFT with serial number 1 from operator's to receiver account.
         * This transaction should be executed on behalf of the `delegatingSpender`,
         * which has an allowance to send serial 1, and should end up with `SUCCESS`.
         */
        // Generate TransactionId from spender's account id in order,
        // for the transaction to be executed on behalf of the spender.
        TransactionId onBehalfOfTransactionId3 = TransactionId.generate(delegatingSpenderAccountId);

        TransactionReceipt approvedSendReceipt3 = new TransferTransaction()
            .addApprovedNftTransfer(example2Nft1, OPERATOR_ID, receiverAccountId2)
            .setTransactionId(onBehalfOfTransactionId3)
            .freezeWith(client)
            .sign(delegatingSpenderPrivateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Transfer serial 1 on behalf of the spender status:" + approvedSendReceipt3.status);

        /*
         * Step 17:
         * Remove `delegatingSpender` allowance for all of NFT serials.
         */
        TransactionReceipt deleteAllowanceReceipt2 = new AccountAllowanceApproveTransaction()
            .deleteTokenNftAllowanceAllSerials(nftTokenId2, OPERATOR_ID, delegatingSpenderAccountId)
            .execute(client)
            .getReceipt(client);
        System.out.println("Remove spender's allowance for serial 2 - status: " + deleteAllowanceReceipt2.status);

        /*
         * Step 18:
         * Send NFT with serial number 2 from operator's to receiver account.
         * Spender does not have an allowance to send serial 2, should end up with `SPENDER_DOES_NOT_HAVE_ALLOWANCE`.
         */
        // Generate TransactionId from spender's account id in order,
        // for the transaction to be executed on behalf of the spender.
        TransactionId onBehalfOfTransactionId4 = TransactionId.generate(delegatingSpenderAccountId);

        try {
            new TransferTransaction()
                .addApprovedNftTransfer(example2Nft2, OPERATOR_ID, receiverAccountId2)
                .setTransactionId(onBehalfOfTransactionId4)
                .freezeWith(client)
                .sign(delegatingSpenderPrivateKey)
                .execute(client)
                .getReceipt(client);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        /*
         * Clean up:
         * Delete created accounts and tokens.
         */
        new TokenWipeTransaction()
            .setTokenId(nftTokenId)
            .addSerial(1)
            .setAccountId(receiverAccountId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        new TokenWipeTransaction()
            .setTokenId(nftTokenId2)
            .addSerial(1)
            .addSerial(3)
            .setAccountId(receiverAccountId2)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(spenderAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(spenderPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(receiverAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(receiverPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(delegatingSpenderAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(delegatingSpenderPrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(receiverAccountId2)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(receiverPrivateKey2)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(nftTokenId)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(nftTokenId2)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
