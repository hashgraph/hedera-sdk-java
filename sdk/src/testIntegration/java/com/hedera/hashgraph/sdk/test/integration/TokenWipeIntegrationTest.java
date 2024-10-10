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
package com.hedera.hashgraph.sdk.test.integration;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenGrantKycTransaction;
import com.hedera.hashgraph.sdk.TokenMintTransaction;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenWipeTransaction;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenWipeIntegrationTest {
    @Test
    @DisplayName("Can wipe accounts balance")
    void canWipeAccountsBalance() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
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
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setAmount(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }


    @Test
    @DisplayName("Can wipe accounts NFTs")
    void canWipeAccountsNfts() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
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
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(NftMetadataGenerator.generate((byte) 10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var serialsToTransfer = mintReceipt.serials.subList(0, 4);
            var transfer = new TransferTransaction();
            for (var serial : serialsToTransfer) {
                transfer.addNftTransfer(tokenId.nft(serial), testEnv.operatorId, accountId);
            }
            transfer.execute(testEnv.client).getReceipt(testEnv.client);

            new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setSerials(serialsToTransfer)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        }
    }


    @Test
    @DisplayName("Cannot wipe accounts NFTs if the account doesn't own them")
    void cannotWipeAccountsNftsIfNotOwned() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
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
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(NftMetadataGenerator.generate((byte) 10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var serialsToTransfer = mintReceipt.serials.subList(0, 4);
            // don't transfer them

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                new TokenWipeTransaction()
                    .setTokenId(tokenId)
                    .setAccountId(accountId)
                    .setSerials(serialsToTransfer)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.ACCOUNT_DOES_NOT_OWN_WIPED_NFT.toString());

        }
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when account ID is not set")
    void cannotWipeAccountsBalanceWhenAccountIDIsNotSet() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
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
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                new TokenWipeTransaction()
                    .setTokenId(tokenId)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_ACCOUNT_ID.toString());

        }
    }

    @Test
    @DisplayName("Cannot wipe accounts balance when token ID is not set")
    void cannotWipeAccountsBalanceWhenTokenIDIsNotSet() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
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
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
                new TokenWipeTransaction()
                    .setAccountId(accountId)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_TOKEN_ID.toString());

        }
    }

    @Test
    @DisplayName("Can wipe accounts balance when amount is not set")
    void canWipeAccountsBalanceWhenAmountIsNotSet() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var key = PrivateKey.generateED25519();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
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
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            new TokenAssociateTransaction()
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .addTokenTransfer(tokenId, testEnv.operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);


            var receipt = new TokenWipeTransaction()
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertThat(receipt.status).isEqualTo(Status.SUCCESS);

        }
    }
}
