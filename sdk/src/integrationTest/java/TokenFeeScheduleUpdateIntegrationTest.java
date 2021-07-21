import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;



class TokenFeeScheduleUpdateIntegrationTest {
    @Test
    @DisplayName("Can update token fees")
    void canUpdateToken() {
        assertDoesNotThrow(() -> {
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

            @Var var info = new TokenInfoQuery()
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
            assertEquals(testEnv.operatorKey.toString(), info.adminKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.freezeKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.wipeKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.kycKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.supplyKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.feeScheduleKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);
            assertEquals(info.customFees.size(), 0);

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

            new TokenFeeScheduleUpdateTransaction()
                .setTokenId(tokenId)
                .setCustomFees(customFees)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new TokenInfoQuery()
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
            assertEquals(testEnv.operatorKey.toString(), info.adminKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.freezeKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.wipeKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.kycKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.supplyKey.toString());
            assertEquals(testEnv.operatorKey.toString(), info.feeScheduleKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);

            var fees = info.customFees;
            assertEquals(fees.size(), 2);
            int fixedCount = 0, fractionalCount = 0;
            for(var fee : fees) {
                if(fee instanceof CustomFixedFee) {
                    fixedCount++;
                    var fixed = (CustomFixedFee)fee;
                    assertEquals(fixed.getAmount(), 10);
                    assertEquals(fixed.getFeeCollectorAccountId(), testEnv.operatorId);
                    assertNull(fixed.getDenominatingTokenId());
                }
                else if(fee instanceof CustomFractionalFee) {
                    fractionalCount++;
                    var fractional = (CustomFractionalFee)fee;
                    assertEquals(fractional.getNumerator(), 1);
                    assertEquals(fractional.getDenominator(), 20);
                    assertEquals(fractional.getMin(), 1);
                    assertEquals(fractional.getMax(), 10);
                    assertEquals(fractional.getFeeCollectorAccountId(), testEnv.operatorId);
                }
            }
            assertEquals(fixedCount, 1);
            assertEquals(fractionalCount, 1);

            testEnv.close(tokenId);
        });
    }

    @Test
    @DisplayName("Cannot update fee schedule with any key other than fee schedule key")
    void cannotUpdateWithAnyOtherKey() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTreasuryAccountId(testEnv.operatorId)
                .setAdminKey(testEnv.operatorKey)
                .setFeeScheduleKey(PrivateKey.generate())
                .setFreezeDefault(false)
                .execute(testEnv.client);

            var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

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

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new TokenFeeScheduleUpdateTransaction()
                    .setTokenId(tokenId)
                    .setCustomFees(customFees)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.client.close();
        });
    }
}
