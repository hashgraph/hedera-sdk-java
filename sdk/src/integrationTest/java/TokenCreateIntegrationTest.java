import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenCreateIntegrationTest {
    @Test
    @DisplayName("Can create token with operator as all keys")
    void canCreateTokenWithOperatorAsAllKeys() {
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
                .setFeeScheduleKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can create token with minimal properties set")
    void canCreateTokenWithMinimalPropertiesSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token name is not set")
    void cannotCreateTokenWhenTokenNameIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_NAME.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token symbol is not set")
    void cannotCreateTokenWhenTokenSymbolIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_SYMBOL.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID is not set")
    void cannotCreateTokenWhenTokenTreasuryAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_TREASURY_ACCOUNT_FOR_TOKEN.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID does not sign transaction")
    void cannotCreateTokenWhenTokenTreasuryAccountIDDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(AccountId.fromString("0.0.3"))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when admin key does not sign transaction")
    void cannotCreateTokenWhenAdminKeyDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.client.close();
        });
    }

    @Disabled
    @Test
    @DisplayName("Can create token with custom fees")
    void canCreateTokenWithCustomFees() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .addCustomFee(new CustomFixedFee().setAmount(10).setFeeCollectorAccountId(testEnv.operatorId))
                .addCustomFee(new CustomFractionalFee().setNumerator(1).setDenominator(20).setMin(1).setMax(10))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
            testEnv.client.close();
        });
    }

    @Disabled
    @DisplayName("Can create NFT")
    void canCreateNfts() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFreezeKey(testEnv.operatorKey)
                .setWipeKey(testEnv.operatorKey)
                .setKycKey(testEnv.operatorKey)
                .setSupplyKey(testEnv.operatorKey)
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            testEnv.client.close();
        });
    }
}
