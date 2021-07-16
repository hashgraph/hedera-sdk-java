import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete token")
    void canDeleteToken() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
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
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            new TokenDeleteTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenId(tokenId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Can delete token with only admin key set")
    void canDeleteTokenWithOnlyAdminKeySet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Cannot delete token when admin key does not sign transaction")
    void cannotDeleteTokenWhenAdminKeyDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var key = PrivateKey.generate();

            var response = new TokenCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(key)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenDeleteTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenId(tokenId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            new TokenDeleteTransaction()
                .setTokenId(tokenId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot delete token when token ID is not set")
    void cannotDeleteTokenWhenTokenIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenDeleteTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_TOKEN_ID.toString()));

            testEnv.cleanUpAndClose();
        });
    }
}
