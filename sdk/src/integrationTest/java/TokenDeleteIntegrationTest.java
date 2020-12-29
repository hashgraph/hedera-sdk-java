import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete token")
    void canDeleteToken() {
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

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can delete token with only admin key set")
    void canDeleteTokenWithOnlyAdminKeySet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(operatorId)
                .setAdminKey(operatorKey)
                .execute(client);

            var tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot delete token when admin key does not sign transaction")
    void cannotDeleteTokenWhenAdminKeyDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(operatorId)
                .setAdminKey(key)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            var tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenDeleteTransaction()
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setTokenId(tokenId)
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot delete token when token ID is not set")
    void cannotDeleteTokenWhenTokenIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenDeleteTransaction()
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

            client.close();
        });
    }
}
