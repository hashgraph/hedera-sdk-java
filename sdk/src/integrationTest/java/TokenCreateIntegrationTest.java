import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class TokenCreateIntegrationTest {
    @Test
    @DisplayName("Can create token with operator as all keys")
    void canCreateTokenWithOperatorAsAllKeys() {
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
    @DisplayName("Can create token with minimal properties set")
    void canCreateTokenWithMinimalPropertiesSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(operatorId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token name is not set")
    void cannotCreateTokenWhenTokenNameIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(operatorId)
                    .execute(client)
                    .getReceipt(client);

            });

            assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_NAME.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token symbol is not set")
    void cannotCreateTokenWhenTokenSymbolIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTreasuryAccountId(operatorId)
                    .execute(client)
                    .getReceipt(client);

            });

            assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_SYMBOL.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID is not set")
    void cannotCreateTokenWhenTokenTreasuryAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .execute(client)
                    .getReceipt(client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_TREASURY_ACCOUNT_FOR_TOKEN.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID does not sign transaction")
    void cannotCreateTokenWhenTokenTreasuryAccountIDDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(AccountId.fromString("0.0.3"))
                    .execute(client)
                    .getReceipt(client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when admin key does not sign transaction")
    void cannotCreateTokenWhenAdminKeyDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(operatorId)
                    .setAdminKey(key)
                    .execute(client)
                    .getReceipt(client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            client.close();
        });
    }
}
