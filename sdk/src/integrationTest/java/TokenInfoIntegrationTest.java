import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenInfoIntegrationTest {
    @Test
    @DisplayName("Can query token info when all keys are different")
    void canQueryTokenInfoWhenAllKeysAreDifferent() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key1 = PrivateKey.generate();
            var key2 = PrivateKey.generate();
            var key3 = PrivateKey.generate();
            var key4 = PrivateKey.generate();
            var key5 = PrivateKey.generate();

            var response = new TokenCreateTransaction()
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
                .setNodeAccountIds(testEnv.nodeAccountIds)
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

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can query token with minimal properties")
    void canQueryTokenInfoWhenTokenIsCreatedWithMinimalProperties() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var info = new TokenInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
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

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Get cost of token info query")
    void getCostQueryTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId);

            var cost = infoQuery.getCost(testEnv.client);

            infoQuery.setQueryPayment(cost).execute(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Get cost of token info query, with big max")
    void getCostBigMaxQueryTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(testEnv.client);

            infoQuery.setQueryPayment(cost).execute(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can query token info when all keys are different")
    void getCostSmallMaxTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = infoQuery.getCost(testEnv.client);

            var error = assertThrows(RuntimeException.class, () -> {
                infoQuery.execute(testEnv.client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TokenInfoQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Throws insufficient transaction fee error")
    void getCostInsufficientTxFeeQueryTokenInfo() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var infoQuery = new TokenInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(testEnv.client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            testEnv.client.close();
        });
    }
}



