import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var tokenId = new TokenCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .tokenId;

            // TODO: we lose this account
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
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
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


    @Test
    @DisplayName("Can create token with custom fees")
    void canCreateTokenWithCustomFees() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var customFees = new ArrayList<CustomFee>();
            customFees.add(new CustomFixedFee()
                .setAmount(10)
                .setFeeCollectorAccountId(testEnv.operatorId)
            );
            customFees.add(new CustomFractionalFee()
                .setNumerator(1)
                .setDenominator(20)
                .setMin(1)
                .setMax(10)
                .setFeeCollectorAccountId(testEnv.operatorId)
            );

            var tokenId = new TokenCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setCustomFees(customFees)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
            testEnv.client.close();
        });
    }

    private static List<CustomFee> createFixedFeeList(int count, AccountId feeCollector) {
        var feeList = new ArrayList<CustomFee>();
        for(int i = 0; i < count; i++) {
            feeList.add(new CustomFixedFee()
                .setAmount(10)
                .setFeeCollectorAccountId(feeCollector));
        }
        return feeList;
    }

    private static List<CustomFee> createFractionalFeeList(int count, AccountId feeCollector) {
        var feeList = new ArrayList<CustomFee>();
        for(int i = 0; i < count; i++) {
            feeList.add(new CustomFractionalFee()
                .setNumerator(1)
                .setDenominator(20)
                .setMin(1)
                .setMax(10)
                .setFeeCollectorAccountId(feeCollector));
        }
        return feeList;
    }


    @Test
    @DisplayName("Cannot create custom fee list with > 10 entries")
    void cannotCreateMoreThanTenCustomFees() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setCustomFees(createFixedFeeList(11, testEnv.operatorId))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.CUSTOM_FEES_LIST_TOO_LONG.toString()));

            testEnv.client.close();
        });
    }


    @Test
    @DisplayName("Can create custom fee list with 10 fixed fees")
    void canCreateTenFixedFees() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setCustomFees(createFixedFeeList(10, testEnv.operatorId))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }


    @Test
    @DisplayName("Can create custom fee list with 10 fractional fees")
    void canCreateTenFractionalFees() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setCustomFees(createFractionalFeeList(10, testEnv.operatorId))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create a token with a custom fee where min > max")
    void cannotCreateMinGreaterThanMax() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setCustomFees(Collections.singletonList(new CustomFractionalFee()
                        .setNumerator(1)
                        .setDenominator(3)
                        .setMin(3)
                        .setMax(2)
                        .setFeeCollectorAccountId(testEnv.operatorId)))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.FRACTIONAL_FEE_MAX_AMOUNT_LESS_THAN_MIN_AMOUNT.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create a token with invalid fee collector account ID")
    void cannotCreateInvalidFeeCollector() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setCustomFees(Collections.singletonList(new CustomFixedFee()
                        .setAmount(1)))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_CUSTOM_FEE_COLLECTOR.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot create a token with a negative custom fee")
    void cannotCreateNegativeFee() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setCustomFees(Collections.singletonList(new CustomFixedFee()
                        .setAmount(-1)
                        .setFeeCollectorAccountId(testEnv.operatorId)))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.CUSTOM_FEE_MUST_BE_POSITIVE.toString()));

            testEnv.client.close();
        });
    }


    @Test
    @DisplayName("Cannot create custom fee with 0 denominator")
    void cannotCreateZeroDenominator() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenCreateTransaction()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTokenName("ffff")
                    .setTokenSymbol("F")
                    .setTreasuryAccountId(testEnv.operatorId)
                    .setCustomFees(Collections.singletonList(new CustomFractionalFee()
                        .setNumerator(1)
                        .setDenominator(0)
                        .setMin(1)
                        .setMax(10)
                        .setFeeCollectorAccountId(testEnv.operatorId)))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.FRACTION_DIVIDES_BY_ZERO.toString()));

            testEnv.client.close();
        });
    }

    @Disabled
    @Test
    @DisplayName("Can create NFT")
    void canCreateNfts() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withThrowawayAccount();

            var response = new TokenCreateTransaction()
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
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

            testEnv.cleanUpAndClose(tokenId);
        });
    }
}
