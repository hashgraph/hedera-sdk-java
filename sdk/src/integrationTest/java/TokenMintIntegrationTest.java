import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenMintIntegrationTest {
    @Test
    @DisplayName("Can mint tokens")
    void canMintTokens() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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

            var receipt = new TokenMintTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setAmount(10)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertEquals(receipt.totalSupply, 1000000 + 10);

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Cannot mint tokens when token ID is not set")
    void cannotMintTokensWhenTokenIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenMintTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot mint tokens when amount is not set")
    void cannotMintTokensWhenAmountIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenMintTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenId(tokenId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_MINT_AMOUNT.toString()));

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Cannot mint tokens when supply key does not sign transaction")
    void cannotMintTokensWhenSupplyKeyDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setDecimals(3)
                    .setInitialSupply(1000000)
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(testEnv.operatorKey)
                    .setFreezeKey(testEnv.operatorKey)
                    .setWipeKey(testEnv.operatorKey)
                    .setKycKey(testEnv.operatorKey)
                    .setSupplyKey(key)
                    .setFreezeDefault(false)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client)
                    .tokenId
            );

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenMintTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenId(tokenId)
                    .setAmount(10)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.cleanUpAndClose(tokenId, accountId, key);
        });
    }

    @Disabled
    @Test
    @DisplayName("Can mint NFTs")
    void canMintNfts() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var tokenId = Objects.requireNonNull(
                new TokenCreateTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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

            var receipt = new TokenMintTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setMetadata(NftMetadataGenerator.generate((byte)10))
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertEquals(receipt.serials.size(), 10);

            testEnv.cleanUpAndClose(tokenId);
        });
    }
}
