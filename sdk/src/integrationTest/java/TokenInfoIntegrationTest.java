import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenInfoIntegrationTest {
    @Test
    @DisplayName("Can query token info when all keys are different")
    void canQueryTokenInfoWhenAllKeysAreDifferent() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var key1 = PrivateKey.generate();
            var key2 = PrivateKey.generate();
            var key3 = PrivateKey.generate();
            var key4 = PrivateKey.generate();
            var key5 = PrivateKey.generate();

            var response = new TokenCreateTransaction()
                //setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(key1)
                .setFreezeKey(key2)
                .setWipeKey(key3)
                .setKycKey(key4)
                .setSupplyKey(key5)
                .setFreezeDefault(false)
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var info = new TokenInfoQuery()
                .setTokenId(tokenId)
                .execute(testEnv.client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "ffff");
            assertEquals(info.symbol, "F");
            assertEquals(info.decimals, 3);
            assertEquals(testEnv.operatorId, info.treasuryAccountId);
            assertNotNull(info.adminKey);
            assertNotNull(info.freezeKey);
            assertNotNull(info.wipeKey);
            assertNotNull(info.kycKey);
            assertNotNull(info.supplyKey);
            assertEquals(key1.getPublicKey().toString(), info.adminKey.toString());
            assertEquals(key2.getPublicKey().toString(), info.freezeKey.toString());
            assertEquals(key3.getPublicKey().toString(), info.wipeKey.toString());
            assertEquals(key4.getPublicKey().toString(), info.kycKey.toString());
            assertEquals(key5.getPublicKey().toString(), info.supplyKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);
            assertEquals(info.tokenType, TokenType.FUNGIBLE_COMMON);
            assertEquals(info.supplyType, TokenSupplyType.INFINITE);

            new TokenDeleteTransaction()
                .setTokenId(tokenId)
                .freezeWith(testEnv.client)
                .sign(key1)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Can query token with minimal properties")
    void canQueryTokenInfoWhenTokenIsCreatedWithMinimalProperties() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var info = new TokenInfoQuery()
                .setTokenId(tokenId)
                .execute(testEnv.client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "ffff");
            assertEquals(info.symbol, "F");
            assertEquals(info.decimals, 0);
            assertEquals(info.totalSupply, 0);
            assertEquals(testEnv.operatorId, info.treasuryAccountId);
            assertNull(info.adminKey);
            assertNull(info.freezeKey);
            assertNull(info.wipeKey);
            assertNull(info.kycKey);
            assertNull(info.supplyKey);
            assertNull(info.defaultFreezeStatus);
            assertNull(info.defaultKycStatus);
            assertEquals(info.tokenType, TokenType.FUNGIBLE_COMMON);
            assertEquals(info.supplyType, TokenSupplyType.INFINITE);


            // TODO: we lose this account
            testEnv.client.close();
        });
    }

    @Disabled
    @Test
    @DisplayName("Can query NFT")
    void canQueryNfts() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setSupplyType(TokenSupplyType.FINITE)
                .setMaxSupply(5000)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var mintReceipt = new TokenMintTransaction()
                .setTokenId(tokenId)
                .setMetadata(NftMetadataGenerator.generate((byte)10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            assertEquals(mintReceipt.serials.size(), 10);

            var info = new TokenInfoQuery()
                .setTokenId(tokenId)
                .execute(testEnv.client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "ffff");
            assertEquals(info.symbol, "F");
            assertEquals(info.decimals, 0);
            assertEquals(info.totalSupply, 10);
            assertEquals(testEnv.operatorId, info.treasuryAccountId);
            assertNull(info.adminKey);
            assertNull(info.freezeKey);
            assertNull(info.wipeKey);
            assertNull(info.kycKey);
            assertNotNull(info.supplyKey);
            assertNull(info.defaultFreezeStatus);
            assertNull(info.defaultKycStatus);
            assertEquals(info.tokenType, TokenType.NON_FUNGIBLE_UNIQUE);
            assertEquals(info.supplyType, TokenSupplyType.FINITE);
            assertEquals(info.maxSupply, 5000);

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Get cost of token info query")
    void getCostQueryTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setTokenId(tokenId);

            var cost = infoQuery.getCost(testEnv.client);

            infoQuery.setQueryPayment(cost).execute(testEnv.client);

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Get cost of token info query, with big max")
    void getCostBigMaxQueryTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setTokenId(tokenId)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(testEnv.client);

            infoQuery.setQueryPayment(cost).execute(testEnv.client);

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Can query token info when all keys are different")
    void getCostSmallMaxTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setTokenId(tokenId)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = infoQuery.getCost(testEnv.client);

            var error = assertThrows(RuntimeException.class, () -> {
                infoQuery.execute(testEnv.client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TokenInfoQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Throws insufficient transaction fee error")
    void getCostInsufficientTxFeeQueryTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setTokenId(tokenId)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(testEnv.client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            testEnv.cleanUpAndClose(tokenId);
        });
    }
}



