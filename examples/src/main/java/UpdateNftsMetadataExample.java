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

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.NftId;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenNftInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateNftsTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UpdateNftsMetadataExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private static final PrivateKey METADATA_KEY = PrivateKey.generateED25519();

    private static final byte[] INITIAL_METADATA = new byte[]{1};

    private static final byte[] UPDATED_METADATA = new byte[]{1, 2};

    private Client client;

    public static void main(String[] args) throws Exception {
        UpdateNftsMetadataExample example = new UpdateNftsMetadataExample();

        // demonstrate with a mutable token (the one that has an admin key)
        example.updateNftsMetadata(example.getMutableTokenCreateTransaction());

        // demonstrate with an immutable token (the one that doesn't have an admin key)
        example.updateNftsMetadata(example.getImmutableTokenCreateTransaction());

        example.cleanUp();
    }

    private UpdateNftsMetadataExample() throws Exception {
        client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
    }

    private void updateNftsMetadata(TokenCreateTransaction tokenCreateTransaction) throws Exception {
        //Create a non-fungible token (NFT) with the metadata key field set
        var tokenCreateResponse = tokenCreateTransaction.sign(OPERATOR_KEY).execute(client);
        var tokenCreateReceipt = tokenCreateResponse.getReceipt(client);
        System.out.println("Status of token create transaction: " + tokenCreateReceipt.status);

        //Get the token ID of the token that was created
        var tokenId = tokenCreateReceipt.tokenId;
        System.out.println("Token id: " + tokenId);

        //Query for the token information stored in consensus node state to see that the metadata key is set
        var tokenInfo = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Token metadata key: " + tokenInfo.metadataKey);

        //Mint the first NFT and set the initial metadata for the NFT
        var tokenMintTransaction = new TokenMintTransaction()
            .setMetadata(List.of(INITIAL_METADATA))
            .setTokenId(tokenId);

        tokenMintTransaction.getMetadata().forEach(metadata -> {
            System.out.println("Set metadata: " + Arrays.toString(metadata));
        });

        var tokenMintResponse = tokenMintTransaction.execute(client);
        
        // Get receipt for mint token transaction
        var tokenMintReceipt = tokenMintResponse.getReceipt(client);
        System.out.println("Status of token mint transaction: " + tokenMintReceipt.status);

        var nftSerials = tokenMintReceipt.serials;
        // Check that metadata on the NFT was set correctly
        getMetadataList(client, tokenId, nftSerials).forEach(metadata -> {
            System.out.println("Metadata after mint: " + Arrays.toString(metadata));
        });

        //Create an account to send the NFT to
        var accountCreateTransaction = new AccountCreateTransaction()
            .setKey(OPERATOR_KEY)
            .setMaxAutomaticTokenAssociations(10)
            .execute(client);

        var newAccountId = accountCreateTransaction.getReceipt(client).accountId;

        //Associate the NFT to the account only if the account does not have any automatic token association slots open. When we created the account in the previous step we set automatic token associations to 10 so I should have 10 slots open to receive a token
        //new TokenAssociateTransaction()
            //.setAccountId(newAccountId)
            //.setTokenIds(Collections.singletonList(tokenId))
            //.freezeWith(client)
            //.sign(OPERATOR_KEY)
            //.execute(client)
            //.getReceipt(client);

        //Transfer the NFT to the new account
        new TransferTransaction()
            .addNftTransfer(tokenId.nft(nftSerials.get(0)), OPERATOR_ID, newAccountId)
            .execute(client);

        // Update nft's metadata
        var tokenUpdateNftsTransaction = new TokenUpdateNftsTransaction()
            .setTokenId(tokenId)
            .setSerials(nftSerials)
            .setMetadata(UPDATED_METADATA)
            .freezeWith(client);

        System.out.println("Updated metadata: " + Arrays.toString(tokenUpdateNftsTransaction.getMetadata()));
        var tokenUpdateNftsResponse = tokenUpdateNftsTransaction.sign(METADATA_KEY).execute(client);
        
        // Get receipt for update nfts metadata transaction
        var tokenUpdateNftsReceipt = tokenUpdateNftsResponse.getReceipt(client);
        System.out.println("Status of token update nfts metadata transaction: " + tokenUpdateNftsReceipt.status);

        // Check that metadata for the NFT was updated correctly
        getMetadataList(client, tokenId, nftSerials).forEach(metadata -> {
            System.out.println("Metadata after update: " + Arrays.toString(metadata));
        });
    }

    private TokenCreateTransaction getMutableTokenCreateTransaction() {
        System.out.println("Creating a mutable token..");

        // Create a mutable token with a metadata key
        return new TokenCreateTransaction()
            .setTokenName("Mutable")
            .setTokenSymbol("MUT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAdminKey(OPERATOR_KEY)
            .setSupplyKey(OPERATOR_KEY)
            .setMetadataKey(METADATA_KEY)
            .freezeWith(client);
    }

    private TokenCreateTransaction getImmutableTokenCreateTransaction() {
        System.out.println("Creating an immutable token..");

        // Create an immutable token with a metadata key
        return new TokenCreateTransaction()
            .setTokenName("Immutable")
            .setTokenSymbol("IMUT")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setTreasuryAccountId(OPERATOR_ID)
            .setSupplyKey(OPERATOR_KEY)
            .setMetadataKey(METADATA_KEY)
            .freezeWith(client);
    }

    private void cleanUp() throws Exception {
        client.close();
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
