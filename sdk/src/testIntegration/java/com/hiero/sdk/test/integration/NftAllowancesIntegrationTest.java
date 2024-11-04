/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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

import com.hiero.sdk.AccountAllowanceApproveTransaction;
import com.hiero.sdk.AccountAllowanceDeleteTransaction;
import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.NftId;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.ReceiptStatusException;
import com.hiero.sdk.Status;
import com.hiero.sdk.TokenAssociateTransaction;
import com.hiero.sdk.TokenCreateTransaction;
import com.hiero.sdk.TokenId;
import com.hiero.sdk.TokenMintTransaction;
import com.hiero.sdk.TokenNftInfoQuery;
import com.hiero.sdk.TokenType;
import com.hiero.sdk.TransactionId;
import com.hiero.sdk.TransferTransaction;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class NftAllowancesIntegrationTest {
    @Test
    @DisplayName("Cannot transfer on behalf of `spender` account without allowance approval")
    void cannotTransferWithoutAllowanceApproval() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var spenderKey = PrivateKey.generateED25519();
            var spenderAccountId = new AccountCreateTransaction()
                .setKey(spenderKey)
                .setInitialBalance(new Hbar(2))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var receiverKey = PrivateKey.generateED25519();
            var receiverAccountId = new AccountCreateTransaction()
                .setKey(receiverKey)
                .setMaxAutomaticTokenAssociations(10)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            TokenId nftTokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .freezeWith(testEnv.client)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            new TokenAssociateTransaction()
                .setAccountId(spenderAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client);

            var serials = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .addMetadata("asd".getBytes(StandardCharsets.UTF_8))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .serials;

            var nft1 = new NftId(nftTokenId, serials.get(0));

            var onBehalfOfTransactionId = TransactionId.generate(spenderAccountId);

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> new TransferTransaction()
                .addApprovedNftTransfer(nft1, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)).withMessageContaining(Status.SPENDER_DOES_NOT_HAVE_ALLOWANCE.toString());

        }
    }

    @Test
    @DisplayName("Cannot transfer on behalf of `spender` account after removing the allowance approval")
    void cannotTransferAfterAllowanceRemove() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var spenderKey = PrivateKey.generateED25519();
            var spenderAccountId = new AccountCreateTransaction()
                .setKey(spenderKey)
                .setInitialBalance(new Hbar(2))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var receiverKey = PrivateKey.generateED25519();
            var receiverAccountId = new AccountCreateTransaction()
                .setKey(receiverKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            TokenId nftTokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .freezeWith(testEnv.client)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            new TokenAssociateTransaction()
                .setAccountId(spenderAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client);

            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(receiverKey)
                .execute(testEnv.client);

            var serials = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .addMetadata("asd1".getBytes(StandardCharsets.UTF_8))
                .addMetadata("asd2".getBytes(StandardCharsets.UTF_8))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .serials;

            var nft1 = new NftId(nftTokenId, serials.get(0));
            var nft2 = new NftId(nftTokenId, serials.get(1));

            new AccountAllowanceApproveTransaction()
                .approveTokenNftAllowance(nft1, testEnv.operatorId, spenderAccountId)
                .approveTokenNftAllowance(nft2, testEnv.operatorId, spenderAccountId)
                .execute(testEnv.client);

            new AccountAllowanceDeleteTransaction()
                .deleteAllTokenNftAllowances(nft2, testEnv.operatorId)
                .execute(testEnv.client);

            var onBehalfOfTransactionId = TransactionId.generate(spenderAccountId);

            new TransferTransaction()
                .addApprovedNftTransfer(nft1, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var info = new TokenNftInfoQuery()
                .setNftId(nft1)
                .execute(testEnv.client);
            assertThat(info.get(0).accountId).isEqualTo(receiverAccountId);

            var onBehalfOfTransactionId2 = TransactionId.generate(spenderAccountId);

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> new TransferTransaction()
                .addApprovedNftTransfer(nft2, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId2)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)).withMessageContaining(Status.SPENDER_DOES_NOT_HAVE_ALLOWANCE.toString());

        }
    }

    @Test
    @DisplayName("Cannot remove single serial number allowance when the allowance is given for all serials at once")
    void cannotRemoveSingleSerialWhenAllowanceIsGivenForAll() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var spenderKey = PrivateKey.generateED25519();
            var spenderAccountId = new AccountCreateTransaction()
                .setKey(spenderKey)
                .setInitialBalance(new Hbar(2))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var receiverKey = PrivateKey.generateED25519();
            var receiverAccountId = new AccountCreateTransaction()
                .setKey(receiverKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            TokenId nftTokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .freezeWith(testEnv.client)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            new TokenAssociateTransaction()
                .setAccountId(spenderAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client);

            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(receiverKey)
                .execute(testEnv.client);

            var serials = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .addMetadata("asd1".getBytes(StandardCharsets.UTF_8))
                .addMetadata("asd2".getBytes(StandardCharsets.UTF_8))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .serials;

            var nft1 = new NftId(nftTokenId, serials.get(0));
            var nft2 = new NftId(nftTokenId, serials.get(1));

            new AccountAllowanceApproveTransaction()
                .approveTokenNftAllowanceAllSerials(nftTokenId, testEnv.operatorId, spenderAccountId)
                .execute(testEnv.client);

            var onBehalfOfTransactionId = TransactionId.generate(spenderAccountId);

            new TransferTransaction()
                .addApprovedNftTransfer(nft1, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            // hopefully in the future this should end up with a precheck error provided from services
            new AccountAllowanceDeleteTransaction()
                .deleteAllTokenNftAllowances(nft2, testEnv.operatorId)
                .execute(testEnv.client);

            var onBehalfOfTransactionId2 = TransactionId.generate(spenderAccountId);

            new TransferTransaction()
                .addApprovedNftTransfer(nft2, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId2)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var infoNft1 = new TokenNftInfoQuery()
                .setNftId(nft1)
                .execute(testEnv.client);

            var infoNft2 = new TokenNftInfoQuery()
                .setNftId(nft2)
                .execute(testEnv.client);

            assertThat(infoNft1.get(0).accountId).isEqualTo(receiverAccountId);
            assertThat(infoNft2.get(0).accountId).isEqualTo(receiverAccountId);
        }
    }

    @Test
    @DisplayName("Account, which given the allowance for all serials at once, should be able to give allowances for single serial numbers to other accounts")
    void accountGivenAllowanceForAllShouldBeAbleToGiveAllowanceForSingle() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1)){

            var delegatingSpenderKey = PrivateKey.generateED25519();
            var delegatingSpenderAccountId = new AccountCreateTransaction()
                .setKey(delegatingSpenderKey)
                .setInitialBalance(new Hbar(2))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var spenderKey = PrivateKey.generateED25519();
            var spenderAccountId = new AccountCreateTransaction()
                .setKey(spenderKey)
                .setInitialBalance(new Hbar(2))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            var receiverKey = PrivateKey.generateED25519();
            var receiverAccountId = new AccountCreateTransaction()
                .setKey(receiverKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .accountId;

            TokenId nftTokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .freezeWith(testEnv.client)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            new TokenAssociateTransaction()
                .setAccountId(delegatingSpenderAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client);

            new TokenAssociateTransaction()
                .setAccountId(receiverAccountId)
                .setTokenIds(List.of(nftTokenId))
                .freezeWith(testEnv.client)
                .sign(receiverKey)
                .execute(testEnv.client);

            var serials = new TokenMintTransaction()
                .setTokenId(nftTokenId)
                .addMetadata("asd1".getBytes(StandardCharsets.UTF_8))
                .addMetadata("asd2".getBytes(StandardCharsets.UTF_8))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .serials;

            var nft1 = new NftId(nftTokenId, serials.get(0));
            var nft2 = new NftId(nftTokenId, serials.get(1));

            new AccountAllowanceApproveTransaction()
                .approveTokenNftAllowanceAllSerials(nftTokenId, testEnv.operatorId, delegatingSpenderAccountId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new AccountAllowanceApproveTransaction()
                .approveTokenNftAllowance(nft1, testEnv.operatorId, spenderAccountId, delegatingSpenderAccountId)
                .freezeWith(testEnv.client)
                .sign(delegatingSpenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var onBehalfOfTransactionId = TransactionId.generate(spenderAccountId);

            new TransferTransaction()
                .addApprovedNftTransfer(nft1, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var onBehalfOfTransactionId2 = TransactionId.generate(spenderAccountId);

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> new TransferTransaction()
                .addApprovedNftTransfer(nft2, testEnv.operatorId, receiverAccountId)
                .setTransactionId(onBehalfOfTransactionId2)
                .freezeWith(testEnv.client)
                .sign(spenderKey)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)).withMessageContaining(Status.SPENDER_DOES_NOT_HAVE_ALLOWANCE.toString());

            var infoNft1 = new TokenNftInfoQuery()
                .setNftId(nft1)
                .execute(testEnv.client);

            var infoNft2 = new TokenNftInfoQuery()
                .setNftId(nft2)
                .execute(testEnv.client);

            assertThat(infoNft1.get(0).accountId).isEqualTo(receiverAccountId);
            assertThat(infoNft2.get(0).accountId).isEqualTo(testEnv.operatorId);

        }
    }
}
