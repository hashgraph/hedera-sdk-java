/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2021 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk.test.integration;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.CustomFee;
import com.hedera.hashgraph.sdk.CustomFixedFee;
import com.hedera.hashgraph.sdk.CustomFractionalFee;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenFeeScheduleUpdateTransaction;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Objects;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class TokenFeeScheduleUpdateIntegrationTest {
    @Test
    @DisplayName("Can update token fees")
    void canUpdateToken() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

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

            assertThat(info.tokenId).isEqualTo(tokenId);
            assertThat(info.name).isEqualTo("ffff");
            assertThat(info.symbol).isEqualTo("F");
            assertThat(info.decimals).isEqualTo(3);
            assertThat(testEnv.operatorId).isEqualTo(info.treasuryAccountId);
            assertThat(info.adminKey).isNotNull();
            assertThat(info.freezeKey).isNotNull();
            assertThat(info.wipeKey).isNotNull();
            assertThat(info.kycKey).isNotNull();
            assertThat(info.supplyKey).isNotNull();
            assertThat(info.adminKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.freezeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.wipeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.kycKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.supplyKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.feeScheduleKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.defaultFreezeStatus).isNotNull();
            assertThat(info.defaultFreezeStatus).isFalse();
            assertThat(info.defaultKycStatus).isNotNull();
            assertThat(info.defaultKycStatus).isFalse();
            assertThat(info.customFees.size()).isEqualTo(0);

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

            assertThat(info.tokenId).isEqualTo(tokenId);
            assertThat(info.name).isEqualTo("ffff");
            assertThat(info.symbol).isEqualTo("F");
            assertThat(info.decimals).isEqualTo(3);
            assertThat(info.treasuryAccountId).isEqualTo(testEnv.operatorId);
            assertThat(info.adminKey).isNotNull();
            assertThat(info.freezeKey).isNotNull();
            assertThat(info.wipeKey).isNotNull();
            assertThat(info.kycKey).isNotNull();
            assertThat(info.supplyKey).isNotNull();
            assertThat(info.adminKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.freezeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.wipeKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.kycKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.supplyKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.feeScheduleKey.toString()).isEqualTo(testEnv.operatorKey.toString());
            assertThat(info.defaultFreezeStatus).isNotNull();
            assertThat(info.defaultFreezeStatus).isFalse();
            assertThat(info.defaultKycStatus).isNotNull();
            assertThat(info.defaultKycStatus).isFalse();

            var fees = info.customFees;
            assertThat(fees.size()).isEqualTo(2);
            @Var int fixedCount = 0;
            @Var int fractionalCount = 0;
            for (var fee : fees) {
                if (fee instanceof CustomFixedFee) {
                    fixedCount++;
                    var fixed = (CustomFixedFee) fee;
                    assertThat(fixed.getAmount()).isEqualTo(10);
                    assertThat(fixed.getFeeCollectorAccountId()).isEqualTo(testEnv.operatorId);
                    assertThat(fixed.getDenominatingTokenId()).isNull();
                } else if (fee instanceof CustomFractionalFee) {
                    fractionalCount++;
                    var fractional = (CustomFractionalFee) fee;
                    assertThat(fractional.getNumerator()).isEqualTo(1);
                    assertThat(fractional.getDenominator()).isEqualTo(20);
                    assertThat(fractional.getMin()).isEqualTo(1);
                    assertThat(fractional.getMax()).isEqualTo(10);
                    assertThat(fractional.getFeeCollectorAccountId()).isEqualTo(testEnv.operatorId);
                }
            }
            assertThat(fixedCount).isEqualTo(1);
            assertThat(fractionalCount).isEqualTo(1);

        }
    }

    @Test
    @DisplayName("Cannot update fee schedule with any key other than fee schedule key")
    void cannotUpdateWithAnyOtherKey() throws Exception {
        try(var testEnv = new IntegrationTestEnv(1).useThrowawayAccount()){

            var response = new TokenCreateTransaction()
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

            assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
                new TokenFeeScheduleUpdateTransaction()
                    .setTokenId(tokenId)
                    .setCustomFees(customFees)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            }).withMessageContaining(Status.INVALID_SIGNATURE.toString());

        }
    }
}
