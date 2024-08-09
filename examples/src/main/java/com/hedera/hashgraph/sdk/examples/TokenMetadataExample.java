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
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Arrays;
import java.util.Objects;

/**
 * HIP-646: Fungible Token Metadata Field.
 * Addition of the metadata field to Fungible Tokens (FT),
 * taking after the Non-Fungible Token (NFT) metadata field which was added in HIP-17.
 * <p>
 * HIP-765: NFT Collection Token Metadata Field
 * Addition of the metadata field to Non-Fungible Token Class,
 * taking after the individual Non-Fungible Token (NFT) metadata field, which was added in HIP-17.
 * <p>
 * How to set and update token's metadata.
 */
public class TokenMetadataExample {

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
         * Generate keys for future tokens.
         */
        PrivateKey adminPrivateKey = PrivateKey.generateED25519();
        PublicKey adminPublicKey = adminPrivateKey.getPublicKey();

        PrivateKey metadataPrivateKey = PrivateKey.generateED25519();
        PublicKey metadataPublicKey = metadataPrivateKey.getPublicKey();

        /*
         * Step 2:
         * The beginning of the first example (mutable token's metadata).
         * Create a mutable fungible token with a metadata, but without a metadata key.
         */
        System.out.println("Example №1 (mutable token's metadata).");
        byte[] initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var tokenIdMutable = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                // The same flow can be executed with a TokenType.NON_FUNGIBLE_UNIQUE (i.e. HIP-765).
                .setTokenType(TokenType.FUNGIBLE_COMMON)
                .setTreasuryAccountId(OPERATOR_ID)
                .setDecimals(3)
                .setInitialSupply(1_000_000)
                .setAdminKey(adminPublicKey)
                .freezeWith(client)
                .sign(adminPrivateKey)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        System.out.println("Created a mutable token: " + tokenIdMutable);

        /*
         * Step 3:
         * Query and output mutable token info after its creation.
         */
        var mutableTokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenIdMutable)
            .execute(client);

        // Check that metadata was set correctly.
        System.out.println("Mutable token's metadata after creation: " + Arrays.toString(mutableTokenInfoAfterCreation.metadata));

        /*
         * Step 4:
         * Update mutable token's metadata.
         */
        byte[] updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        new TokenUpdateTransaction()
            .setTokenId(tokenIdMutable)
            .setTokenMetadata(updatedTokenMetadata)
            .freezeWith(client)
            .sign(adminPrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 5:
         * Query and output mutable token info after its metadata was updated.
         */
        var mutableTokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenIdMutable)
            .execute(client);

        // check that metadata was updated correctly
        System.out.println("Mutable token's metadata after update: " + Arrays.toString(mutableTokenInfoAfterMetadataUpdate.metadata));

        /*
         * Step 6:
         * The beginning of the second example (immutable token's metadata).
         * Create an immutable fungible token with a metadata key and a metadata.
         */
        System.out.println("Example №2 (immutable token's metadata).");
        var tokenIdImmutable = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                .setTokenType(TokenType.FUNGIBLE_COMMON) // The same flow can be executed with a TokenType.NON_FUNGIBLE_UNIQUE (i.e. HIP-765)
                .setTreasuryAccountId(OPERATOR_ID)
                .setMetadataKey(metadataPublicKey)
                .setDecimals(3)
                .setInitialSupply(1_000_000)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        System.out.println("Created an immutable token: " + tokenIdImmutable);

        /*
         * Step 7:
         * Query and output immutable token info after its creation.
         */
        var immutableTokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenIdImmutable)
            .execute(client);

        // Check that metadata was set correctly.
        System.out.println("Immutable token's metadata after creation: " + Arrays.toString(immutableTokenInfoAfterCreation.metadata));

        /*
         * Step 8:
         * Update immutable token's metadata.
         */
        new TokenUpdateTransaction()
            .setTokenId(tokenIdImmutable)
            .setTokenMetadata(updatedTokenMetadata)
            .freezeWith(client)
            .sign(metadataPrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 5:
         * Query and output immutable token info after its metadata was updated.
         */
        var immutableTokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenIdImmutable)
            .execute(client);

        // Check that metadata was updated correctly.
        System.out.println("Immutable token's metadata after update: " + Arrays.toString(immutableTokenInfoAfterMetadataUpdate.metadata));

        /*
         * Clean up:
         * Delete created mutable token.
         */
        new TokenDeleteTransaction()
            .setTokenId(tokenIdMutable)
            .freezeWith(client)
            .sign(adminPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Example complete!");
    }
}
