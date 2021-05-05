import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenUpdateIntegrationTest {
    @Test
    @DisplayName("Can update token")
    void canUpdateToken() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
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

            @Var var info = new TokenInfoQuery()
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
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.adminKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.freezeKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.wipeKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.kycKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.supplyKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);

            new TokenUpdateTransaction()
                .setTokenId(tokenId)
                .setTokenName("aaaa")
                .setTokenSymbol("A")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new TokenInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .execute(testEnv.client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "aaaa");
            assertEquals(info.symbol, "A");
            assertEquals(info.decimals, 3);
            assertEquals(testEnv.operatorId, info.treasuryAccountId);
            assertNotNull(info.adminKey);
            assertNotNull(info.freezeKey);
            assertNotNull(info.wipeKey);
            assertNotNull(info.kycKey);
            assertNotNull(info.supplyKey);
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.adminKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.freezeKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.wipeKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.kycKey.toString());
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.supplyKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot update immutable token")
    void cannotUpdateImmutableToken() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenUpdateTransaction()
                    .setTokenId(tokenId)
                    .setTokenName("aaaa")
                    .setTokenSymbol("A")
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.TOKEN_IS_IMMUTABLE.toString()));

            testEnv.client.close();
        });
    }
}
