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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * How to reject a token (part of HIP-904).
 */
class TokenRejectExample {

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
        System.out.println("Token Reject (HIP-904) Example Start!");

        /*
         * Step 0:
         * Create and configure the SDK Client.
         */
        Client client = ClientHelper.forName(HEDERA_NETWORK);
        // All generated transactions will be paid by this account and be signed by this key.
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        // Attach logger to the SDK Client.
        client.setLogger(new Logger(LogLevel.valueOf(SDK_LOG_LEVEL)));

        /*
         * Step 1:
         * Generate ED25519 key pairs for accounts.
         */
        System.out.println("Generating ED25519 key pairs...");
        PrivateKey treasuryAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey treasuryAccountPublicKey = treasuryAccountPrivateKey.getPublicKey();
        PrivateKey receiverAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey receiverAccountPublicKey = receiverAccountPrivateKey.getPublicKey();

        /*
         * Step 2:
         * Create accounts for this example.
         */
        System.out.println("Creating treasury and receiver accounts...");
        // Create a treasury account.
        var treasuryAccountId = new AccountCreateTransaction()
            .setKey(treasuryAccountPublicKey)
            .setMaxAutomaticTokenAssociations(100)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        // Create a receiver account with unlimited max auto associations (-1).
        var receiverAccountId = new AccountCreateTransaction()
            .setKey(receiverAccountPublicKey)
            .setMaxAutomaticTokenAssociations(-1)
            .freezeWith(client)
            .sign(receiverAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        /*
         * Step 3:
         * Create tokens for this example.
         */
        System.out.println("Creating FT and NFT...");
        // Create a Fungible Token.
        final int FUNGIBLE_TOKEN_SUPPLY = 1_000_000;
        var fungibleTokenId = new TokenCreateTransaction()
            .setTokenName("Example Fungible Token")
            .setTokenSymbol("EFT")
            .setTokenMemo("I was created for demo")
            .setDecimals(0)
            .setInitialSupply(FUNGIBLE_TOKEN_SUPPLY)
            .setMaxSupply(FUNGIBLE_TOKEN_SUPPLY)
            .setTreasuryAccountId(treasuryAccountId)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(treasuryAccountPublicKey)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        // Create NFT.
        var nonFungibleTokenId = new TokenCreateTransaction()
            .setTokenName("Example NFT")
            .setTokenSymbol("ENFT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(treasuryAccountId)
            .setSupplyType(TokenSupplyType.FINITE)
            .setMaxSupply(3)
            .setAdminKey(treasuryAccountPublicKey)
            .setSupplyKey(treasuryAccountPublicKey)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        /*
         * Step 4:
         * Mint three NFTs.
         */
        System.out.println("Minting three NFTs...");
        var mintReceiptNftToken = new TokenMintTransaction()
            .setTokenId(nonFungibleTokenId)
            .setMetadata(generateNftMetadata((byte) 3))
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        var nftSerials = mintReceiptNftToken.serials;

        /*
         * Step 5:
         * Transfer tokens to the receiver.
         */
        System.out.println("Transferring tokens to the receiver...");
        new TransferTransaction()
            .addTokenTransfer(fungibleTokenId, treasuryAccountId, -1_000)
            .addTokenTransfer(fungibleTokenId, receiverAccountId, 1_000)
            .addNftTransfer(nonFungibleTokenId.nft(nftSerials.get(0)), treasuryAccountId, receiverAccountId)
            .addNftTransfer(nonFungibleTokenId.nft(nftSerials.get(1)), treasuryAccountId, receiverAccountId)
            .addNftTransfer(nonFungibleTokenId.nft(nftSerials.get(2)), treasuryAccountId, receiverAccountId)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 6:
         * Check receiver account balance.
         */
        var receiverAccountBalance = new AccountBalanceQuery()
            .setAccountId(receiverAccountId)
            .execute(client);

        if (receiverAccountBalance.tokens.get(fungibleTokenId) == 1_000) {
            System.out.println("Receiver account has: " + receiverAccountBalance.tokens.get(fungibleTokenId) + " example fungible tokens.");
        } else {
            throw new Exception("Failed to transfer Fungible Token to the receiver account!");
        }

        if (receiverAccountBalance.tokens.get(nonFungibleTokenId) == 3) {
            System.out.println("Receiver account has: " + receiverAccountBalance.tokens.get(nonFungibleTokenId) + " example NFTs.");
        } else {
            throw new Exception("Failed to transfer NFT to the receiver account!");
        }

        /*
         * Step 7:
         * Reject the fungible token.
         */
        System.out.println("Receiver rejects example fungible tokens...");
        new TokenRejectTransaction()
            .setOwnerId(receiverAccountId)
            .addTokenId(fungibleTokenId)
            .freezeWith(client)
            .sign(receiverAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 8:
         * Execute the token reject flow -- reject NFTs.
         */
        System.out.println("Receiver rejects example NFTs...");
        new TokenRejectFlow()
            .setOwnerId(receiverAccountId)
            .setNftIds(List.of(
                nonFungibleTokenId.nft(nftSerials.get(0)),
                nonFungibleTokenId.nft(nftSerials.get(1)),
                nonFungibleTokenId.nft(nftSerials.get(2))
            ))
            .freezeWith(client)
            .sign(receiverAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Step 9:
         * Check receiver account balance after token reject.
         */
        var receiverAccountBalanceAfterTokenReject = new AccountBalanceQuery()
            .setAccountId(receiverAccountId)
            .execute(client);

        if (receiverAccountBalanceAfterTokenReject.tokens.get(fungibleTokenId) == 0) {
            System.out.println("Receiver account has (after rejecting tokens): " + receiverAccountBalanceAfterTokenReject.tokens.get(fungibleTokenId) + " example fungible tokens.");
        } else {
            throw new Exception("Failed to reject Fungible Token!");
        }

        if (receiverAccountBalanceAfterTokenReject.tokens.get(nonFungibleTokenId) == null) {
            System.out.println("Receiver account has (after rejecting tokens): " + receiverAccountBalanceAfterTokenReject.tokens.get(nonFungibleTokenId) + " example NFTs.");
        } else {
            throw new Exception("Failed to reject NFT!");
        }

        /*
         * Step 10:
         * Check treasury account balance after token reject.
         */
        var treasuryAccountBalance = new AccountBalanceQuery()
            .setAccountId(treasuryAccountId)
            .execute(client);

        if (treasuryAccountBalance.tokens.get(fungibleTokenId) == FUNGIBLE_TOKEN_SUPPLY) {
            System.out.println("Treasury account has: " + treasuryAccountBalance.tokens.get(fungibleTokenId) + " example fungible tokens.");
        } else {
            throw new Exception("Failed to transfer Fungible Token to the treasury account during token rejection!");
        }

        if (treasuryAccountBalance.tokens.get(nonFungibleTokenId) == 3) {
            System.out.println("Receiver account has: " + receiverAccountBalance.tokens.get(nonFungibleTokenId) + " example NFTs.");
        } else {
            throw new Exception("Failed to transfer NFT to the treasury account during token rejection!");
        }

        /*
         * Clean up:
         * Delete created accounts and tokens.
         */
        new AccountDeleteTransaction()
            .setAccountId(treasuryAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client);

        new AccountDeleteTransaction()
            .setAccountId(receiverAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(receiverAccountPrivateKey)
            .execute(client);

        new TokenDeleteTransaction()
            .setTokenId(fungibleTokenId)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        new TokenDeleteTransaction()
            .setTokenId(nonFungibleTokenId)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        client.close();

        System.out.println("Token Reject (HIP-904) Example Complete!");
    }

    private static List<byte[]> generateNftMetadata(byte metadataCount) {
        List<byte[]> metadatas = new ArrayList<>();

        for (byte i = 0; i < metadataCount; i++) {
            byte[] md = {i};
            metadatas.add(md);
        }

        return metadatas;
    }

}
