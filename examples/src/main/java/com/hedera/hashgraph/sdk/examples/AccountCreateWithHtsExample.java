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

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * HIP-542: support auto-creation via HTS assets by charging the account creation fee to the payer
 * of the triggering CryptoTransfer.
 * This means a new alias may be given anywhere in the transaction;
 * both in the hbar transfer list, or in an HTS token transfer list. In the latter case,
 * the assessed creation fee will include at least one auto-association slot,
 * since the new account must be associated to its originating HTS assets.
 */
class AccountCreateWithHtsExample {

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
        client.setDefaultMaxTransactionFee(new Hbar(10));

        /*
         * Step 1:
         * Generate keys for future tokens.
         */
        PublicKey operatorPublicKey = OPERATOR_KEY.getPublicKey();

        PrivateKey supplyPrivateKey = PrivateKey.generateECDSA();
        PublicKey supplyPublicKey = supplyPrivateKey.getPublicKey();

        PrivateKey freezePrivateKey = PrivateKey.generateECDSA();
        PublicKey freezePublicKey = freezePrivateKey.getPublicKey();

        PrivateKey wipePrivateKey = PrivateKey.generateECDSA();
        PublicKey wipePublicKey = wipePrivateKey.getPublicKey();

        /*
         * Step 2:
         * The beginning of the first example (with NFT).
         * Create an NFT using the Hedera Token Service.
         */
        System.out.println("Example №1 (NFT).");

        // IPFS content identifiers for the NFT metadata.
        String[] CIDs = new String[] {
            "QmNPCiNA3Dsu3K5FxDPMG5Q3fZRwVTg14EXA92uqEeSRXn",
            "QmZ4dgAgt8owvnULxnKxNe8YqpavtVCXmc1Lt2XajFpJs9",
            "QmPzY5GxevjyfMUF5vEAjtyRoigzWp47MiKAtLBduLMC1T",
            "Qmd3kGgSrAwwSrhesYcY7K54f3qD7MDo38r7Po2dChtQx5",
            "QmWgkKz3ozgqtnvbCLeh7EaR1H8u5Sshx3ZJzxkcrT3jbw",
        };

        TokenCreateTransaction nftCreateTx = new TokenCreateTransaction()
            .setTokenName("HIP-542 Example Collection")
            .setTokenSymbol("HIP-542")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setDecimals(0)
            .setInitialSupply(0)
            .setMaxSupply(CIDs.length)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(operatorPublicKey)
            .setFreezeKey(freezePublicKey)
            .setWipeKey(wipePublicKey)
            .setSupplyKey(supplyPublicKey)
            .freezeWith(client);

        // Sign the transaction with the operator key.
        TokenCreateTransaction nftCreateTxSign = nftCreateTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network.
        TransactionResponse nftCreateSubmit = nftCreateTxSign.execute(client);

        // Get transaction receipt information.
        TransactionReceipt nftCreateRx = nftCreateSubmit.getReceipt(client);
        TokenId nftTokenId = nftCreateRx.tokenId;
        System.out.println("Created NFT with token id: " + nftTokenId);

        /*
         * Step 3:
         * Mint NFTs.
         */
        TransactionReceipt[] nftCollection = new TransactionReceipt[CIDs.length];
        for (int i = 0; i < CIDs.length; i++) {
            byte[] nftMetadata = CIDs[i].getBytes();
            TokenMintTransaction mintTx = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .setMetadata(List.of(nftMetadata))
                .freezeWith(client);

            TokenMintTransaction mintTxSign = mintTx.sign(supplyPrivateKey);
            TransactionResponse mintTxSubmit = mintTxSign.execute(client);

            nftCollection[i] = mintTxSubmit.getReceipt(client);

            System.out.println("Created NFT " + nftTokenId + " with serial: " + nftCollection[i].serials.get(0));
        }

        long exampleNftId = nftCollection[0].serials.get(0);

        /*
         * Step 4:
         * Create an ECDSA public key alias.
         */
        System.out.println("Creating a new account...");

        PrivateKey privateKey = PrivateKey.generateECDSA();
        PublicKey publicKey = privateKey.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now they are virtually always 0 and 0.
        AccountId aliasAccountId = publicKey.toAccountId(0, 0);

        System.out.println("New account ID: " + aliasAccountId);
        System.out.println("Just the aliasKey: " + aliasAccountId.aliasKey);

        /*
         * Step 5:
         * Transfer the NFT to the public key alias using the transfer transaction.
         */
        TransferTransaction nftTransferTx = new TransferTransaction()
            .addNftTransfer(nftTokenId.nft(exampleNftId), OPERATOR_ID, aliasAccountId)
            .freezeWith(client);

        // Sign the transaction with the operator key.
        TransferTransaction nftTransferTxSign = nftTransferTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network.
        TransactionResponse nftTransferSubmit = nftTransferTxSign.execute(client);

        // Get transaction receipt information here.
        nftTransferSubmit.getReceipt(client);

        /*
         * Step 6:
         * Get the new account ID from the child record.
         */
        List<TokenNftInfo> nftInfo = new TokenNftInfoQuery()
            .setNftId(nftTokenId.nft(exampleNftId))
            .execute(client);

        String nftOwnerAccountId = nftInfo.get(0).accountId.toString();
        System.out.println("Current owner account id: " + nftOwnerAccountId);

        /*
         * Step 7:
         * Show the normal account ID of account which owns the NFT.
         */
        String accountIdString = new AccountInfoQuery()
            .setAccountId(aliasAccountId)
            .execute(client)
            .accountId.toString();

        System.out.println("The normal account ID of the given alias: " + accountIdString);

        /*
         * Step 8:
         * Validate that account ID value from the child record is equal to normal account ID value from the query.
         */
        if (nftOwnerAccountId.equals(accountIdString)) {
            System.out.println("The NFT owner accountId matches the accountId created with the HTS.");
        } else {
            throw new Exception("The two account IDs does not match.");
        }

        /*
         * Step 9:
         * The beginning of the second example (with Fungible Token).
         * Create a fungible HTS token using the Hedera Token Service.
         */
        System.out.println("Example №2 (Fungible Token).");

        TokenCreateTransaction tokenCreateTx = new TokenCreateTransaction()
            .setTokenName("HIP-542 Token")
            .setTokenSymbol("H542")
            .setInitialSupply(10_000) // Total supply = 10000 / 10 ^ 2
            .setDecimals(2)
            .setTokenType(TokenType.FUNGIBLE_COMMON)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAutoRenewAccountId(OPERATOR_ID)
            .setAdminKey(operatorPublicKey)
            .setWipeKey(wipePrivateKey)
            .freezeWith(client);

        // Sign the transaction with the operator key.
        TokenCreateTransaction tokenCreateTxSign = tokenCreateTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network.
        TransactionResponse tokenCreateSubmit = tokenCreateTxSign.execute(client);

        // Get transaction receipt information.
        TransactionReceipt tokenCreateRx = tokenCreateSubmit.getReceipt(client);
        TokenId tokenId = tokenCreateRx.tokenId;
        System.out.println("Created token with token id: " + tokenId);

        /*
         * Step 10:
         * Create an ECDSA public key alias.
         */
        System.out.println("Creating a new account...");

        PrivateKey privateKey2 = PrivateKey.generateECDSA();
        PublicKey publicKey2 = privateKey2.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now, they are virtually always 0 and 0.
        AccountId aliasAccountId2 = publicKey2.toAccountId(0, 0);

        System.out.println("New account ID: " + aliasAccountId2);
        System.out.println("Just the aliasKey: " + aliasAccountId2.aliasKey);

        /*
         * Step 11:
         * Transfer the fungible token to the public key alias.
         */
        TransferTransaction tokenTransferTx = new TransferTransaction()
            .addTokenTransfer(tokenId, OPERATOR_ID, -10)
            .addTokenTransfer(tokenId, aliasAccountId2, 10)
            .freezeWith(client);

        // Sign the transaction with the operator key.
        TransferTransaction tokenTransferTxSign = tokenTransferTx.sign(OPERATOR_KEY);

        // Submit the transaction to the Hedera network.
        TransactionResponse tokenTransferSubmit = tokenTransferTxSign.execute(client);

        // Get transaction receipt information.
        tokenTransferSubmit.getReceipt(client);

        /*
         * Step 12:
         * Get the new account ID from the child record.
         */
        String accountId2String = new AccountInfoQuery()
            .setAccountId(aliasAccountId2)
            .execute(client)
            .accountId.toString();

        System.out.println("The normal account ID of the given alias: " + accountId2String);

        /*
         * Step 13:
         * Show the normal account ID of account which owns the NFT.
         */
        AccountBalance accountBalances = new AccountBalanceQuery()
            .setAccountId(aliasAccountId2)
            .execute(client);

        /*
         * Step 14:
         * Validate token balance of newly created account.
         */
        int tokenBalanceAccountId2 = accountBalances.tokens.get(tokenId).intValue();
        if (tokenBalanceAccountId2 == 10) {
            System.out.println("Account is created successfully using HTS 'TransferTransaction'");
        } else {
            throw new Exception("Creating account with HTS using public key alias failed");
        }

        /*
         * Clean up:
         * Delete created accounts and tokens.
         */
        var accountId = AccountId.fromString(accountIdString);

        new TokenWipeTransaction()
            .setTokenId(nftTokenId)
            .addSerial(exampleNftId)
            .setAccountId(accountId)
            .freezeWith(client)
            .sign(wipePrivateKey)
            .execute(client)
            .getReceipt(client);

        var accountId2 = AccountId.fromString(accountId2String);

        Map<TokenId, Long> accountId2TokensBeforeWipe = new AccountBalanceQuery()
            .setAccountId(accountId2)
            .execute(client)
            .tokens;

        System.out.println("Account Id 2 token balance (before wipe): " + accountId2TokensBeforeWipe.get(tokenId));

        new TokenWipeTransaction()
            .setTokenId(tokenId)
            .setAmount(accountId2TokensBeforeWipe.get(tokenId))
            .setAccountId(accountId2)
            .freezeWith(client)
            .sign(wipePrivateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(accountId2)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(privateKey2)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(nftTokenId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(tokenId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
