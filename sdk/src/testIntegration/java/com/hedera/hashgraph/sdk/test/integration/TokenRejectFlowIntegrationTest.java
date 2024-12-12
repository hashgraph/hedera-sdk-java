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
package com.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hiero.sdk.AccountBalanceQuery;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.TokenAssociateTransaction;
import com.hiero.sdk.TokenDeleteTransaction;
import com.hiero.sdk.TokenMintTransaction;
import com.hiero.sdk.TokenNftInfoQuery;
import com.hiero.sdk.TokenRejectFlow;
import com.hiero.sdk.TransferTransaction;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TokenRejectFlowIntegrationTest {

    @Test
    @DisplayName("Can execute TokenReject flow for Fungible Token")
    void canExecuteTokenRejectFlowForFungibleToken() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){
            var ftTokenId = EntityHelper.createFungibleToken(testEnv, 3);
            var receiverAccountKey = PrivateKey.generateED25519();
            var receiverAccountId = EntityHelper.createAccount(testEnv, receiverAccountKey, 0);

            // manually associate ft
            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(Collections.singletonList(ftTokenId))
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // transfer fts to the receiver
            new TransferTransaction()
                .addTokenTransfer(ftTokenId, testEnv.operatorId, -10)
                .addTokenTransfer(ftTokenId, receiverAccountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // execute the token reject flow
            new TokenRejectFlow()
                .setOwnerId(receiverAccountId)
                .addTokenId(ftTokenId)
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // verify the tokens are transferred back to the treasury
            var treasuryAccountBalance = new AccountBalanceQuery()
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            assertThat(treasuryAccountBalance.tokens.get(ftTokenId)).isEqualTo(1_000_000);

            // verify the allowance - should be 0, because TokenRejectFlow dissociates
            assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
                new TransferTransaction()
                    .addTokenTransfer(ftTokenId, testEnv.operatorId, -10)
                    .addTokenTransfer(ftTokenId, receiverAccountId, 10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining("TOKEN_NOT_ASSOCIATED_TO_ACCOUNT");

            new TokenDeleteTransaction()
                .setTokenId(ftTokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can execute TokenReject flow for Fungible Token (Async)")
    void canExecuteTokenRejectFlowForFungibleTokenAsync() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){
            var ftTokenId = EntityHelper.createFungibleToken(testEnv, 3);
            var receiverAccountKey = PrivateKey.generateED25519();
            var receiverAccountId = EntityHelper.createAccount(testEnv, receiverAccountKey, 0);

            // manually associate ft
            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(Collections.singletonList(ftTokenId))
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // transfer fts to the receiver
            new TransferTransaction()
                .addTokenTransfer(ftTokenId, testEnv.operatorId, -10)
                .addTokenTransfer(ftTokenId, receiverAccountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // execute the token reject flow
            new TokenRejectFlow()
                .setOwnerId(receiverAccountId)
                .addTokenId(ftTokenId)
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .executeAsync(testEnv.client).get()
                .getReceipt(testEnv.client);

            // verify the tokens are transferred back to the treasury
            var treasuryAccountBalance = new AccountBalanceQuery()
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            assertThat(treasuryAccountBalance.tokens.get(ftTokenId)).isEqualTo(1_000_000);

            // verify the tokens are not associated with the receiver
            assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
                new TransferTransaction()
                    .addTokenTransfer(ftTokenId, testEnv.operatorId, -10)
                    .addTokenTransfer(ftTokenId, receiverAccountId, 10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining("TOKEN_NOT_ASSOCIATED_TO_ACCOUNT");

            new TokenDeleteTransaction()
                .setTokenId(ftTokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Can execute TokenReject flow for NFT")
    void canExecuteTokenRejectFlowForNft() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){
        var nftTokenId = EntityHelper.createNft(testEnv);
            var receiverAccountKey = PrivateKey.generateED25519();
            var receiverAccountId = EntityHelper.createAccount(testEnv, receiverAccountKey, 0);

            var mintReceiptToken = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .setMetadata(NftMetadataGenerator.generate((byte) 10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftSerials = mintReceiptToken.serials;

            // manually associate bft
            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(Collections.singletonList(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // transfer nfts to the receiver
            new TransferTransaction()
                .addNftTransfer(nftTokenId.nft(nftSerials.get(0)), testEnv.operatorId, receiverAccountId)
                .addNftTransfer(nftTokenId.nft(nftSerials.get(1)), testEnv.operatorId, receiverAccountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // execute the token reject flow
            new TokenRejectFlow()
                .setOwnerId(receiverAccountId)
                .setNftIds(List.of(nftTokenId.nft(nftSerials.get(0)), nftTokenId.nft(nftSerials.get(1))))
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // verify the token is transferred back to the treasury
            var nftTokenIdNftInfo = new TokenNftInfoQuery()
                .setNftId(nftTokenId.nft(nftSerials.get(1)))
                .execute(testEnv.client);

            assertThat(nftTokenIdNftInfo.get(0).accountId).isEqualTo(testEnv.operatorId);

            // verify the tokens are not associated with the receiver
            assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
                new TransferTransaction()
                    .addNftTransfer(nftTokenId.nft(nftSerials.get(1)), testEnv.operatorId, receiverAccountId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining("TOKEN_NOT_ASSOCIATED_TO_ACCOUNT");

            new TokenDeleteTransaction()
                .setTokenId(nftTokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Cannot execute TokenReject flow for NFT when rejecting Only Part Of Owned NFTs")
    void canExecuteTokenRejectFlowForNftWhenRejectingOnlyPartOfOwnedNFTs() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){
            var nftTokenId1 = EntityHelper.createNft(testEnv);
            var receiverAccountKey = PrivateKey.generateED25519();
            var receiverAccountId = EntityHelper.createAccount(testEnv, receiverAccountKey, 0);

            var mintReceiptToken = new TokenMintTransaction()
                .setTokenId(nftTokenId1)
                .setMetadata(NftMetadataGenerator.generate((byte) 10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var nftSerials = mintReceiptToken.serials;

            // manually associate bft
            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(Collections.singletonList(nftTokenId1))
                .freezeWith(testEnv.client)
                .sign(receiverAccountKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // transfer nfts to the receiver
            new TransferTransaction()
                .addNftTransfer(nftTokenId1.nft(nftSerials.get(0)), testEnv.operatorId, receiverAccountId)
                .addNftTransfer(nftTokenId1.nft(nftSerials.get(1)), testEnv.operatorId, receiverAccountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // execute the token reject flow
            assertThatExceptionOfType(Exception.class).isThrownBy(() -> {
                new TokenRejectFlow()
                    .setOwnerId(receiverAccountId)
                    .addNftId(nftTokenId1.nft(nftSerials.get(1)))
                    .freezeWith(testEnv.client)
                    .sign(receiverAccountKey)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining("ACCOUNT_STILL_OWNS_NFTS");

        }
    }
}
