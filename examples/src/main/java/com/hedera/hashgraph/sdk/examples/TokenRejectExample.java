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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TokenRejectExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private TokenRejectExample() {}

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // create a treasury account
        PrivateKey treasuryAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey treasuryAccountPublicKey = treasuryAccountPrivateKey.getPublicKey();
        var treasuryAccountId = new AccountCreateTransaction()
            .setKey(treasuryAccountPublicKey)
            .setMaxAutomaticTokenAssociations(100)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        // create a receiver account with unlimited max auto associations
        PrivateKey receiverAccountPrivateKey = PrivateKey.generateED25519();
        PublicKey receiverAccountPublicKey = receiverAccountPrivateKey.getPublicKey();
        var receiverAccountId = new AccountCreateTransaction()
            .setKey(receiverAccountPublicKey)
            .setMaxAutomaticTokenAssociations(-1)
            .freezeWith(client)
            .sign(receiverAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        // create a fungible token
        var fungibleTokenId = new TokenCreateTransaction()
            .setTokenName("Example Fungible Token")
            .setTokenSymbol("EFT")
            .setTokenMemo("I was created for demo")
            .setDecimals(0)
            .setInitialSupply(1_000_000)
            .setMaxSupply(1_000_000)
            .setTreasuryAccountId(treasuryAccountId)
            .setSupplyType(TokenSupplyType.FINITE)
            .setAdminKey(treasuryAccountPublicKey)
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        // create NFT
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

        // mint 3 NFTs
        var mintReceiptNftToken = new TokenMintTransaction()
            .setTokenId(nonFungibleTokenId)
            .setMetadata(generateNftMetadata((byte) 3))
            .freezeWith(client)
            .sign(treasuryAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        var nftSerials = mintReceiptNftToken.serials;

        // transfer the tokens to the receiver
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

        var receiverAccountBalance = new AccountBalanceQuery()
            .setAccountId(receiverAccountId)
            .execute(client);

        System.out.println("Receiver account has: " + receiverAccountBalance.tokens.get(fungibleTokenId) + " example fungible tokens.");
        System.out.println("Receiver account has: " + receiverAccountBalance.tokens.get(nonFungibleTokenId) + " example NFTs.");

        System.out.println("Receiver rejects example fungible tokens...");

        // reject the fungible token
        new TokenRejectTransaction()
            .setOwnerId(receiverAccountId)
            .addTokenId(fungibleTokenId)
            .freezeWith(client)
            .sign(receiverAccountPrivateKey)
            .execute(client)
            .getReceipt(client);

        System.out.println("Receiver rejects example NFTs...");

        // execute the token reject flow
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

        var receiverAccountBalanceAfterTokenReject = new AccountBalanceQuery()
            .setAccountId(receiverAccountId)
            .execute(client);

        System.out.println("Receiver account has (after executing TokenReject): " + receiverAccountBalanceAfterTokenReject.tokens.get(fungibleTokenId) + " example fungible tokens.");
        System.out.println("Receiver account has (after executing TokenRejectFlow): " + receiverAccountBalanceAfterTokenReject.tokens.get(nonFungibleTokenId) + " example NFTs.");

        var treasuryAccountBalance = new AccountBalanceQuery()
            .setAccountId(treasuryAccountId)
            .execute(client);

        System.out.println("Treasury account has: " + treasuryAccountBalance.tokens.get(fungibleTokenId) + " example fungible tokens.");
        System.out.println("Treasury account has: " + treasuryAccountBalance.tokens.get(nonFungibleTokenId) + " example NFTs.");

        // clean up

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
