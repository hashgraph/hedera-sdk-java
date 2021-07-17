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
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
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

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Test
    @DisplayName("Can create token with minimal properties set")
    void canCreateTokenWithMinimalPropertiesSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount(5);

            var tokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            // we lose this IntegrationTestEnv throwaway account
            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create token when token name is not set")
    void cannotCreateTokenWhenTokenNameIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_NAME.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot create token when token symbol is not set")
    void cannotCreateTokenWhenTokenSymbolIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_SYMBOL.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID is not set")
    void cannotCreateTokenWhenTokenTreasuryAccountIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_TREASURY_ACCOUNT_FOR_TOKEN.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID does not sign transaction")
    void cannotCreateTokenWhenTokenTreasuryAccountIDDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(AccountId.fromString("0.0.3"))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot create token when admin key does not sign transaction")
    void cannotCreateTokenWhenAdminKeyDoesNotSignTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var key = PrivateKey.generate();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setAdminKey(key)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);

            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Disabled
    @Test
    @DisplayName("Can create token with custom fees")
    void canCreateTokenWithCustomFees() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var tokenId = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .addCustomFee(new CustomFixedFee()
                    .setAmount(10)
                    .setFeeCollectorAccountId(testEnv.operatorId))
                .addCustomFee(new CustomFractionalFee()
                    .setNumerator(1)
                    .setDenominator(20)
                    .setMin(1)
                    .setMax(10)
                    .setFeeCollectorAccountId(testEnv.operatorId))
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            testEnv.cleanUpAndClose(tokenId);
        });
    }

    @Disabled
    @Test
    @DisplayName("Can create NFT")
    void canCreateNfts() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
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

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            testEnv.cleanUpAndClose(tokenId);
        });
    }
}
