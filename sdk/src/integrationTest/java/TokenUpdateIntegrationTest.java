import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenUpdateIntegrationTest {
    @Test
    @DisplayName("Can update token")
    void canUpdateToken() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(operatorId)
                .setAdminKey(operatorKey)
                .setFreezeKey(operatorKey)
                .setWipeKey(operatorKey)
                .setKycKey(operatorKey)
                .setSupplyKey(operatorKey)
                .setFreezeDefault(false)
                .execute(client);

            var tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);

            @Var var info = new TokenInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(2))
                .setTokenId(tokenId)
                .execute(client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "ffff");
            assertEquals(info.symbol, "F");
            assertEquals(info.decimals, 3);
            assertEquals(operatorId, info.treasuryAccountId);
            assertNotNull(info.adminKey);
            assertNotNull(info.freezeKey);
            assertNotNull(info.wipeKey);
            assertNotNull(info.kycKey);
            assertNotNull(info.supplyKey);
            assertEquals(operatorKey.toString(), info.adminKey.toString());
            assertEquals(operatorKey.toString(), info.freezeKey.toString());
            assertEquals(operatorKey.toString(), info.wipeKey.toString());
            assertEquals(operatorKey.toString(), info.kycKey.toString());
            assertEquals(operatorKey.toString(), info.supplyKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);

            new TokenUpdateTransaction()
                .setTokenId(tokenId)
                .setTokenName("aaaa")
                .setTokenSymbol("A")
                .execute(client)
                .getReceipt(client);

            info = new TokenInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(2))
                .setTokenId(tokenId)
                .execute(client);

            assertEquals(tokenId, info.tokenId);
            assertEquals(info.name, "aaaa");
            assertEquals(info.symbol, "A");
            assertEquals(info.decimals, 3);
            assertEquals(operatorId, info.treasuryAccountId);
            assertNotNull(info.adminKey);
            assertNotNull(info.freezeKey);
            assertNotNull(info.wipeKey);
            assertNotNull(info.kycKey);
            assertNotNull(info.supplyKey);
            assertEquals(operatorKey.toString(), info.adminKey.toString());
            assertEquals(operatorKey.toString(), info.freezeKey.toString());
            assertEquals(operatorKey.toString(), info.wipeKey.toString());
            assertEquals(operatorKey.toString(), info.kycKey.toString());
            assertEquals(operatorKey.toString(), info.supplyKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot update immutable token")
    void cannotUpdateImmutableToken() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(operatorId)
                .setFreezeDefault(false)
                .execute(client);

            var tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenUpdateTransaction()
                    .setTokenId(tokenId)
                    .setTokenName("aaaa")
                    .setTokenSymbol("A")
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.TOKEN_IS_IMMUTABLE.toString()));

            client.close();
        });
    }
}
