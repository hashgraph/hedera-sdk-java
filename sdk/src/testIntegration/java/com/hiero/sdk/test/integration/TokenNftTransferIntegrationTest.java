// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.hiero.sdk.AccountCreateTransaction;
import com.hiero.sdk.Hbar;
import com.hiero.sdk.PrivateKey;
import com.hiero.sdk.ReceiptStatusException;
import com.hiero.sdk.Status;
import com.hiero.sdk.TokenAssociateTransaction;
import com.hiero.sdk.TokenCreateTransaction;
import com.hiero.sdk.TokenGrantKycTransaction;
import com.hiero.sdk.TokenMintTransaction;
import com.hiero.sdk.TokenType;
import com.hiero.sdk.TokenWipeTransaction;
import com.hiero.sdk.TransactionResponse;
import com.hiero.sdk.TransferTransaction;
import java.util.ArrayList;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenNftTransferIntegrationTest {
    @Test
    @DisplayName("Can transfer NFTs")
    void canTransferNfts() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()) {

            var key = PrivateKey.generateED25519();

            TransactionResponse response = new AccountCreateTransaction()
                    .setKey(key)
                    .setInitialBalance(new Hbar(1))
                    .execute(testEnv.client);

            var accountId = response.getReceipt(testEnv.client).accountId;
            assertThat(accountId).isNotNull();

            response = new TokenCreateTransaction()
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
                    .execute(testEnv.client);

            var tokenId = response.getReceipt(testEnv.client).tokenId;
            assertThat(tokenId).isNotNull();

            var mintReceipt = new TokenMintTransaction()
                    .setTokenId(tokenId)
                    .setMetadata(NftMetadataGenerator.generate((byte) 10))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                    .setAccountId(accountId)
                    .setTokenIds(Collections.singletonList(tokenId))
                    .freezeWith(testEnv.client)
                    .signWithOperator(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            new TokenGrantKycTransaction()
                    .setAccountId(accountId)
                    .setTokenId(tokenId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            var serialsToTransfer = new ArrayList<Long>(mintReceipt.serials.subList(0, 4));
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
    @DisplayName("Cannot transfer NFTs you don't own")
    void cannotTransferUnownedNfts() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()) {

            var key = PrivateKey.generateED25519();

            TransactionResponse response = new AccountCreateTransaction()
                    .setKey(key)
                    .setInitialBalance(new Hbar(1))
                    .execute(testEnv.client);

            var accountId = response.getReceipt(testEnv.client).accountId;
            assertThat(accountId).isNotNull();

            response = new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(testEnv.operatorKey)
                    .setFreezeKey(testEnv.operatorKey)
                    .setWipeKey(testEnv.operatorKey)
                    .setSupplyKey(testEnv.operatorKey)
                    .setFreezeDefault(false)
                    .execute(testEnv.client);

            var tokenId = response.getReceipt(testEnv.client).tokenId;
            assertThat(tokenId).isNotNull();

            var mintReceipt = new TokenMintTransaction()
                    .setTokenId(tokenId)
                    .setMetadata(NftMetadataGenerator.generate((byte) 10))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            new TokenAssociateTransaction()
                    .setAccountId(accountId)
                    .setTokenIds(Collections.singletonList(tokenId))
                    .freezeWith(testEnv.client)
                    .signWithOperator(testEnv.client)
                    .sign(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            var serialsToTransfer = new ArrayList<Long>(mintReceipt.serials.subList(0, 4));
            var transfer = new TransferTransaction();
            for (var serial : serialsToTransfer) {
                // Try to transfer in wrong direction
                transfer.addNftTransfer(tokenId.nft(serial), accountId, testEnv.operatorId);
            }
            transfer.freezeWith(testEnv.client).sign(key);

            assertThatExceptionOfType(ReceiptStatusException.class)
                    .isThrownBy(() -> {
                        transfer.execute(testEnv.client).getReceipt(testEnv.client);
                    })
                    .withMessageContaining(Status.SENDER_DOES_NOT_OWN_NFT_SERIAL_NO.toString());
        }
    }
}
