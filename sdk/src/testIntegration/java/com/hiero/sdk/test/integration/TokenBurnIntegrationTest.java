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
package com.hiero.sdk.test.integration;

import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.PrecheckStatusException;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.ReceiptStatusException;
import com.hiero.sdk.Status;
import com.hiero.sdk.TokenAssociateTransaction;
import com.hiero.sdk.TokenBurnTransaction;
import com.hiero.sdk.TokenCreateTransaction;
import com.hiero.sdk.TokenMintTransaction;
import com.hiero.sdk.TokenType;
import com.hiero.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenBurnIntegrationTest {
    @Test
    @DisplayName("Can burn tokens")
    void canBurnTokens() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var receipt = new TokenBurnTransaction()
                .setAmount(10)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertThat(receipt.totalSupply).isEqualTo(1000000 - 10);

        }
    }

    @Test
    @DisplayName("Cannot burn tokens when token ID is not set")
    void cannotBurnTokensWhenTokenIDIsNotSet() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()) {

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                new TokenBurnTransaction()
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_TOKEN_ID.toString());

        }
    }

    @Test
    @DisplayName("Can burn tokens when amount is not set")
    void canBurnTokensWhenAmountIsNotSet() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var receipt = new TokenBurnTransaction()
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertThat(receipt.status).isEqualTo(Status.SUCCESS);

        }
    }

    @Test
    @DisplayName("Cannot burn tokens when supply key does not sign transaction")
    void cannotBurnTokensWhenSupplyKeyDoesNotSignTransaction() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(PrivateKey.generate())
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                new TokenBurnTransaction()
                    .setTokenId(tokenId)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_SIGNATURE.toString());

        }
    }

    @Test
    @DisplayName("Can burn NFTs")
    void canBurnNfts() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var createReceipt = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var tokenId = Objects.requireNonNull(createReceipt.tokenId);

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(NftMetadataGenerator.generate((byte) 10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenBurnTransaction()
                .setSerials(mintReceipt.serials.subList(0, 4))
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }

    @Test
    @DisplayName("Cannot burn NFTs when NFT is not owned by treasury")
    void cannotBurnNftsWhenNftIsNotOwned() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var createReceipt = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var tokenId = Objects.requireNonNull(createReceipt.tokenId);

            var serials = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(NftMetadataGenerator.generate((byte) 1))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .serials;

            var key = PrivateKey.generateED25519();

            var accountId = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .addNftTransfer(tokenId.nft(serials.get(0)), testEnv.operatorId, accountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                new TokenBurnTransaction()
                    .setSerials(serials)
                    .setTokenId(tokenId)
                    .execute(testEnv.client)
                        .getReceipt(testEnv.client);
            }).withMessageContaining(Status.TREASURY_MUST_OWN_BURNED_NFT.toString());

        }
    }
}
