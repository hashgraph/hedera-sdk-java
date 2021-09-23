import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.CustomFee;
import com.hedera.hashgraph.sdk.CustomFixedFee;
import com.hedera.hashgraph.sdk.CustomFractionalFee;
import com.hedera.hashgraph.sdk.CustomRoyaltyFee;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenCreateIntegrationTest {
    private static List<CustomFee> createFixedFeeList(int count, AccountId feeCollector) {
        var feeList = new ArrayList<CustomFee>();
        for (int i = 0; i < count; i++) {
            feeList.add(new CustomFixedFee()
                .setAmount(10)
                .setFeeCollectorAccountId(feeCollector));
        }
        return feeList;
    }

    private static List<CustomFee> createFractionalFeeList(int count, AccountId feeCollector) {
        var feeList = new ArrayList<CustomFee>();
        for (int i = 0; i < count; i++) {
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
    @DisplayName("Can create token with operator as all keys")
    void canCreateTokenWithOperatorAsAllKeys() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

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

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Can create token with minimal properties set")
    @SuppressWarnings("UnusedVariable")
    void canCreateTokenWithMinimalPropertiesSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount(new Hbar(10));

        var tokenId = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTreasuryAccountId(testEnv.operatorId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        // we lose this IntegrationTestEnv throwaway account
        testEnv.client.close();
    }

    @Test
    @DisplayName("Cannot create token when token name is not set")
    void cannotCreateTokenWhenTokenNameIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        });

        assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_NAME.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot create token when token symbol is not set")
    void cannotCreateTokenWhenTokenSymbolIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTreasuryAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        });

        assertTrue(error.getMessage().contains(Status.MISSING_TOKEN_SYMBOL.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID is not set")
    void cannotCreateTokenWhenTokenTreasuryAccountIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        });

        assertTrue(error.getMessage().contains(Status.INVALID_TREASURY_ACCOUNT_FOR_TOKEN.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot create token when token treasury account ID does not sign transaction")
    void cannotCreateTokenWhenTokenTreasuryAccountIDDoesNotSignTransaction() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(AccountId.fromString("0.0.3"))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

        });

        assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot create token when admin key does not sign transaction")
    void cannotCreateTokenWhenAdminKeyDoesNotSignTransaction() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

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

        testEnv.close();
    }

    @Test
    @DisplayName("Can create token with custom fees")
    void canCreateTokenWithCustomFees() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

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
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setCustomFees(customFees)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;
        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot create custom fee list with > 10 entries")
    void cannotCreateMoreThanTenCustomFees() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setAdminKey(testEnv.operatorKey)
                .setTreasuryAccountId(testEnv.operatorId)
                .setCustomFees(createFixedFeeList(11, testEnv.operatorId))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.CUSTOM_FEES_LIST_TOO_LONG.toString()));

        testEnv.close();
    }


    @Test
    @DisplayName("Can create custom fee list with 10 fixed fees")
    void canCreateTenFixedFees() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setCustomFees(createFixedFeeList(10, testEnv.operatorId))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        testEnv.close(tokenId);
    }


    @Test
    @DisplayName("Can create custom fee list with 10 fractional fees")
    void canCreateTenFractionalFees() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setAdminKey(testEnv.operatorKey)
            .setTreasuryAccountId(testEnv.operatorId)
            .setCustomFees(createFractionalFeeList(10, testEnv.operatorId))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        testEnv.close(tokenId);
    }

    @Test
    @DisplayName("Cannot create a token with a custom fee where min > max")
    void cannotCreateMinGreaterThanMax() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
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

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot create a token with invalid fee collector account ID")
    void cannotCreateInvalidFeeCollector() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setAdminKey(testEnv.operatorKey)
                .setTreasuryAccountId(testEnv.operatorId)
                .setCustomFees(Collections.singletonList(new CustomFixedFee()
                    .setAmount(1)))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_CUSTOM_FEE_COLLECTOR.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot create a token with a negative custom fee")
    void cannotCreateNegativeFee() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setAdminKey(testEnv.operatorKey)
                .setTreasuryAccountId(testEnv.operatorId)
                .setCustomFees(Collections.singletonList(new CustomFixedFee()
                    .setAmount(-1)
                    .setFeeCollectorAccountId(testEnv.operatorId)))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.CUSTOM_FEE_MUST_BE_POSITIVE.toString()));

        testEnv.close();
    }


    @Test
    @DisplayName("Cannot create custom fee with 0 denominator")
    void cannotCreateZeroDenominator() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var error = assertThrows(ReceiptStatusException.class, () -> {
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
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

        testEnv.close();
    }


    @Test
    @DisplayName("Can create NFT")
    void canCreateNfts() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

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

        testEnv.close(tokenId);
    }


    @Test
    @DisplayName("Can create NFT with royalty fee")
    void canCreateRoyaltyFee() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var tokenId = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setTreasuryAccountId(testEnv.operatorId)
            .setSupplyKey(testEnv.operatorKey)
            .setAdminKey(testEnv.operatorKey)
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setCustomFees(Collections.singletonList(new CustomRoyaltyFee()
                .setNumerator(1)
                .setDenominator(10)
                .setFallbackFee(new CustomFixedFee().setHbarAmount(new Hbar(1)))
                .setFeeCollectorAccountId(testEnv.operatorId)))
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        testEnv.close(tokenId);
    }
}
