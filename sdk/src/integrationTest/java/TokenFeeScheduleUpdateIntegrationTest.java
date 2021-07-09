import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@Disabled
class TokenFeeScheduleUpdateIntegrationTest {
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
                .setFeeScheduleKey(testEnv.operatorKey)
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
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.feeScheduleKey.toString());
            assertNotNull(info.defaultFreezeStatus);
            assertFalse(info.defaultFreezeStatus);
            assertNotNull(info.defaultKycStatus);
            assertFalse(info.defaultKycStatus);
            assertEquals(info.customFees.size(), 0);

            new TokenFeeScheduleUpdateTransaction()
                .setTokenId(tokenId)
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
                .getReceipt(testEnv.client);

            info = new TokenInfoQuery()
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
            assertEquals(testEnv.operatorKey.getPublicKey().toString(), info.feeScheduleKey.toString());
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

            testEnv.client.close();
        });
    }
}