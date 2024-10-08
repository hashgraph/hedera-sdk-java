/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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

package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 * TokenCreateParams for token create method
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenCreateParams extends JSONRPC2Param {
    private Optional<String> name;
    private Optional<String> symbol;
    private Optional<Long> decimals;
    private Optional<Long> initialSupply;
    private Optional<String> treasuryAccountId;
    private Optional<String> adminKey;
    private Optional<String> kycKey;
    private Optional<String> freezeKey;
    private Optional<String> wipeKey;
    private Optional<String> supplyKey;
    private Optional<String> feeScheduleKey;
    private Optional<String> pauseKey;
    private Optional<String> metadataKey;
    private Optional<Boolean> freezeDefault;
    private Optional<Long> expirationTime;
    private Optional<String> autoRenewAccountId;
    private Optional<Long> autoRenewPeriod;
    private Optional<String> memo;
    private Optional<String> tokenType;
    private Optional<String> supplyType;
    private Optional<Long> maxSupply;
    private Optional<List<CustomFee>> customFees;
    private Optional<String> metadata;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        var parsedName = Optional.ofNullable((String) jrpcParams.get("name"));
        var parsedSymbol = Optional.ofNullable((String) jrpcParams.get("symbol"));
        var parsedDecimals = Optional.ofNullable((Long) jrpcParams.get("decimals"));
        Optional<Long> parsedInitialSupply;
        try {
            parsedInitialSupply = Optional.ofNullable((Long) jrpcParams.get("initialSupply"));
        } catch (Exception e) {
            parsedInitialSupply = Optional.of(((BigInteger) jrpcParams.get("initialSupply")).longValue());
        }
        var parsedTreasuryAccountId = Optional.ofNullable((String) jrpcParams.get("treasuryAccountId"));
        var parsedAdminKey = Optional.ofNullable((String) jrpcParams.get("adminKey"));
        var parsedKycKey = Optional.ofNullable((String) jrpcParams.get("kycKey"));
        var parsedFreezeKey = Optional.ofNullable((String) jrpcParams.get("freezeKey"));
        var parsedWipeKey = Optional.ofNullable((String) jrpcParams.get("wipeKey"));
        var parsedSupplyKey = Optional.ofNullable((String) jrpcParams.get("supplyKey"));
        var parsedFeeScheduleKey = Optional.ofNullable((String) jrpcParams.get("feeScheduleKey"));
        var parsedPauseKey = Optional.ofNullable((String) jrpcParams.get("pauseKey"));
        var parsedMetadataKey = Optional.ofNullable((String) jrpcParams.get("metadataKey"));
        var parsedFreezeDefault = Optional.ofNullable((Boolean) jrpcParams.get("freezeDefault"));
        var parsedExpirationTime = Optional.ofNullable((Long) jrpcParams.get("expirationTime"));
        var parsedAutoRenewAccountId = Optional.ofNullable((String) jrpcParams.get("autoRenewAccountId"));
        Optional<Long> parsedAutoRenewPeriod;
        try {
            parsedAutoRenewPeriod = Optional.ofNullable((Long) jrpcParams.get("autoRenewPeriod"));
        } catch (Exception e) {
            parsedAutoRenewPeriod = Optional.of(((BigInteger) jrpcParams.get("autoRenewPeriod")).longValue());
        }
        var parsedMemo = Optional.ofNullable((String) jrpcParams.get("memo"));
        var parsedTokenType = Optional.ofNullable((String) jrpcParams.get("tokenType"));
        var parsedSupplyType = Optional.ofNullable((String) jrpcParams.get("supplyType"));
        Optional<Long> parsedMaxSupply;
        try {
            parsedMaxSupply = Optional.ofNullable((Long) jrpcParams.get("maxSupply"));
        } catch (Exception e) {
            parsedMaxSupply = Optional.of(((BigInteger) jrpcParams.get("maxSupply")).longValue());
        }
        var parsedMetadata = Optional.ofNullable((String) jrpcParams.get("metadata"));

        Optional<List<CustomFee>> parsedCustomFees = Optional.empty();
        if (jrpcParams.containsKey("customFees")) {
            JSONArray jsonArray = (JSONArray) jrpcParams.get("customFees");
            List<CustomFee> customFees = new ArrayList<>();

            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                CustomFee customFee = new CustomFee().parse(jsonObject);
                customFees.add(customFee);
            }
            parsedCustomFees = Optional.of(customFees);
        }

        Optional<CommonTransactionParams> parsedCommonTransactionParams = Optional.empty();
        if (jrpcParams.containsKey("commonTransactionParams")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("commonTransactionParams");
            parsedCommonTransactionParams = Optional.of(CommonTransactionParams.parse(jsonObject));
        }

        return new TokenCreateParams(
                parsedName,
                parsedSymbol,
                parsedDecimals,
                parsedInitialSupply,
                parsedTreasuryAccountId,
                parsedAdminKey,
                parsedKycKey,
                parsedFreezeKey,
                parsedWipeKey,
                parsedSupplyKey,
                parsedFeeScheduleKey,
                parsedPauseKey,
                parsedMetadataKey,
                parsedFreezeDefault,
                parsedExpirationTime,
                parsedAutoRenewAccountId,
                parsedAutoRenewPeriod,
                parsedMemo,
                parsedTokenType,
                parsedSupplyType,
                parsedMaxSupply,
                parsedCustomFees,
                parsedMetadata,
                parsedCommonTransactionParams);
    }
}
