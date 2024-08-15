/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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
import java.util.List;
import java.util.Objects;

/**
 * How to update NFTs' metadata (HIP-657).
 */
class UpdateNftsMetadataExample {

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
        System.out.println("Update Nfts Metadata Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        PublicKey operatorKeyPublic = OPERATOR_KEY.getPublicKey();

        /*
         * Step 1:
         * Generate ED25519 key pair (Metadata Key).
         */
        System.out.println("Generating ED25519 key pair...(metadata key).");
        PrivateKey metadataPrivateKey = PrivateKey.generateED25519();
        PublicKey metadataPublicKey = metadataPrivateKey.getPublicKey();

        /*
         * Step 2:
         * The beginning of the first example (mutable token's metadata).
         *
         * Create a non-fungible token (NFT) with the metadata key field set.
         */
        System.out.println("The beginning of the first example (mutable token's metadata).");
        byte[] initialMetadata = new byte[]{1};
        System.out.println("Creating mutable NFT with the metadata key field set...");
        var mutableTokenCreateTransaction = new TokenCreateTransaction()
            .setTokenName("Mutable")
            .setTokenSymbol("MUT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAdminKey(operatorKeyPublic)
            .setSupplyKey(operatorKeyPublic)
            .setMetadataKey(metadataPublicKey)
            .freezeWith(client);

        var mutableTokenCreateResponse = mutableTokenCreateTransaction.sign(OPERATOR_KEY).execute(client);
        var mutableTokenCreateReceipt = mutableTokenCreateResponse.getReceipt(client);
        // Get the token ID of the token that was created.
        var mutableTokenId = mutableTokenCreateReceipt.tokenId;
        System.out.println("Created mutable NFT with token ID: " + mutableTokenId);

        /*
         * Step 3:
         * Query for the mutable token information stored in consensus node state to see that the Metadata Key is set.
         */
        var mutableTokenInfo = new TokenInfoQuery()
            .setTokenId(mutableTokenId)
            .execute(client);

        System.out.println("Mutable NFT metadata key: " + mutableTokenInfo.metadataKey);

        /*
         * Step 4:
         * Mint the first NFT and set the initial metadata for the NFT.
         */
        System.out.println("Minting NFTs...");
        var mutableTokenMintTransaction = new TokenMintTransaction()
            .setMetadata(List.of(initialMetadata))
            .setTokenId(mutableTokenId);

        mutableTokenMintTransaction.getMetadata().forEach(metadata -> {
            System.out.println("Setting metadata: " + Arrays.toString(metadata));
        });

        var mutableTokenMintResponse = mutableTokenMintTransaction.execute(client);

        // Get receipt for mint token transaction.
        var mutableTokenMintReceipt = mutableTokenMintResponse.getReceipt(client);
        System.out.println("Mint transaction was complete with status: " + mutableTokenMintReceipt.status);

        var mutableNftSerials = mutableTokenMintReceipt.serials;

        // Check that metadata on the NFT was set correctly.
        getMetadataList(client, mutableTokenId, mutableNftSerials).forEach(metadata -> {
            System.out.println("Metadata after mint: " + Arrays.toString(metadata));
        });

        /*
         * Step 5:
         * Create an account to send the NFT to.
         */
        System.out.println("Creating new account...");
        var accountCreateTransaction = new AccountCreateTransaction()
            .setKey(operatorKeyPublic)
            // If the account does not have any automatic token association,
            // slots open ONLY then associate the NFT to the account.
            .setMaxAutomaticTokenAssociations(10)
            .execute(client);

        var newAccountId = accountCreateTransaction.getReceipt(client).accountId;
        System.out.println("Created new account with ID: " + newAccountId);

        /*
         * Step 6:
         * Transfer the NFT to the new account.
         */
        System.out.println("Transferring the NFT to the new account...");
        new TransferTransaction()
            .addNftTransfer(mutableTokenId.nft(mutableNftSerials.get(0)), OPERATOR_ID, newAccountId)
            .execute(client);

        /*
         * Step 7:
         * Update NFTs' metadata.
         */
        byte[] updatedMetadata = new byte[]{1, 2};
        System.out.println("Updating NFTs' metadata...");
        var tokenUpdateNftsTransaction = new TokenUpdateNftsTransaction()
            .setTokenId(mutableTokenId)
            .setSerials(mutableNftSerials)
            .setMetadata(updatedMetadata)
            .freezeWith(client);

        System.out.println("Updated NFTs' metadata: " + Arrays.toString(tokenUpdateNftsTransaction.getMetadata()));
        var tokenUpdateNftsResponse = tokenUpdateNftsTransaction.sign(metadataPrivateKey).execute(client);

        // Get receipt for update NFTs metadata transaction.
        var tokenUpdateNftsReceipt = tokenUpdateNftsResponse.getReceipt(client);
        System.out.println("Token update nfts metadata transaction was complete with status: " + tokenUpdateNftsReceipt.status);

        // Check that metadata for the NFT was updated correctly.
        getMetadataList(client, mutableTokenId, mutableNftSerials).forEach(metadata -> {
            System.out.println("NFTs' metadata after update: " + Arrays.toString(metadata));
        });

        /*
         * Step 8:
         * The beginning of the second example (immutable token's metadata).
         *
         * Create a non-fungible token (NFT) with the metadata key field set.
         */
        System.out.println("The beginning of the second example (immutable token's metadata).");
        System.out.println("Creating immutable NFT with the metadata key field set...");
        var immutableTokenCreateTransaction = new TokenCreateTransaction()
            .setTokenName("Immutable")
            .setTokenSymbol("IMUT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyKey(operatorKeyPublic)
            .setMetadataKey(metadataPublicKey)
            .freezeWith(client);

        var immutableTokenCreateResponse = immutableTokenCreateTransaction.sign(OPERATOR_KEY).execute(client);
        var immutableTokenCreateReceipt = immutableTokenCreateResponse.getReceipt(client);
        // Get the token ID of the token that was created.
        var immutableTokenId = immutableTokenCreateReceipt.tokenId;
        System.out.println("Created immutable NFT with token ID: " + immutableTokenId);

        /*
         * Step 9:
         * Query for the mutable token information stored in consensus node state to see that the metadata key is set.
         */
        var immutableTokenInfo = new TokenInfoQuery()
            .setTokenId(immutableTokenId)
            .execute(client);

        System.out.println("Immutable NFT metadata key: " + immutableTokenInfo.metadataKey);

        /*
         * Step 10:
         * Mint the first NFT and set the initial metadata for the NFT.
         */
        System.out.println("Minting NFTs...");
        var immutableTokenMintTransaction = new TokenMintTransaction()
            .setMetadata(List.of(initialMetadata))
            .setTokenId(immutableTokenId);

        immutableTokenMintTransaction.getMetadata().forEach(metadata -> {
            System.out.println("Setting metadata: " + Arrays.toString(metadata));
        });

        var immutableTokenMintResponse = immutableTokenMintTransaction.execute(client);

        // Get receipt for mint token transaction.
        var immutableTokenMintReceipt = immutableTokenMintResponse.getReceipt(client);
        System.out.println("Mint transaction was complete with status: " + immutableTokenMintReceipt.status);

        var immutableNftSerials = immutableTokenMintReceipt.serials;
        // Check that metadata on the NFT was set correctly.
        getMetadataList(client, immutableTokenId, immutableNftSerials).forEach(metadata -> {
            System.out.println("Metadata after mint: " + Arrays.toString(metadata));
        });

        /*
         * Step 11:
         * Create an account to send the NFT to.
         */
        System.out.println("Creating new account...");
        var newAccountCreateTransaction = new AccountCreateTransaction()
            .setKey(operatorKeyPublic)
            // If the account does not have any automatic token association,
            // slots open ONLY then associate the NFT to the account.
            .setMaxAutomaticTokenAssociations(10)
            .execute(client);

        var newAccountId2 = newAccountCreateTransaction.getReceipt(client).accountId;
        System.out.println("Created new account with ID: " + newAccountId2);

        /*
         * Step 12:
         * Transfer the NFT to the new account.
         */
        System.out.println("Transferring the NFT to the new account...");
        new TransferTransaction()
            .addNftTransfer(immutableTokenId.nft(immutableNftSerials.get(0)), OPERATOR_ID, newAccountId2)
            .execute(client);

        /*
         * Step 13:
         * Update NFTs' metadata.
         */
        System.out.println("Updating NFTs' metadata...");
        var immutableTokenUpdateNftsTransaction = new TokenUpdateNftsTransaction()
            .setTokenId(immutableTokenId)
            .setSerials(immutableNftSerials)
            .setMetadata(updatedMetadata)
            .freezeWith(client);

        System.out.println("Updated NFTs' metadata: " + Arrays.toString(immutableTokenUpdateNftsTransaction.getMetadata()));
        var immutableTokenUpdateNftsResponse = immutableTokenUpdateNftsTransaction.sign(metadataPrivateKey).execute(client);

        // Get receipt for update NFTs metadata transaction.
        var immutableTokenUpdateNftsReceipt = immutableTokenUpdateNftsResponse.getReceipt(client);
        System.out.println("Token update nfts metadata transaction was complete with status: " + immutableTokenUpdateNftsReceipt.status);

        // Check that metadata for the NFT was updated correctly.
        getMetadataList(client, immutableTokenId, immutableNftSerials).forEach(metadata -> {
            System.out.println("NFTs' metadata after update: " + Arrays.toString(metadata));
        });

        /*
         * Clean up:
         * Delete created mutable token.
         */
        new TokenDeleteTransaction()
            .setTokenId(mutableTokenId)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Update Nfts Metadata Example Complete!");
    }

    private static List<byte[]> getMetadataList(Client client, TokenId tokenId, List<Long> nftSerials) {
        return nftSerials.stream()
            .map(serial -> new NftId(tokenId, serial))
            .flatMap(nftId -> {
                try {
                    return new TokenNftInfoQuery()
                        .setNftId(nftId)
                        .execute(client).stream();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            })
            .map(tokenNftInfo -> tokenNftInfo.metadata)
            .toList();
    }
}
