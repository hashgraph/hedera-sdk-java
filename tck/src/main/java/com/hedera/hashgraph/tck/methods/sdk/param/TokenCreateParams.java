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
    private Optional<String> initialSupply;
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
    private Optional<String> expirationTime;
    private Optional<String> autoRenewAccountId;
    private Optional<String> autoRenewPeriod;
    private Optional<String> memo;
    private Optional<String> tokenType;
    private Optional<String> supplyType;
    private Optional<String> maxSupply;
    private Optional<List<CustomFee>> customFees;
    private Optional<String> metadata;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        var parsedName = Optional.ofNullable((String) jrpcParams.get("name"));
        var parsedSymbol = Optional.ofNullable((String) jrpcParams.get("symbol"));
        var parsedDecimals = Optional.ofNullable((Long) jrpcParams.get("decimals"));
        var parsedInitialSupply = Optional.ofNullable((String) jrpcParams.get("initialSupply"));
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
        var parsedExpirationTime = Optional.ofNullable((String) jrpcParams.get("expirationTime"));
        var parsedAutoRenewAccountId = Optional.ofNullable((String) jrpcParams.get("autoRenewAccountId"));
        var parsedAutoRenewPeriod = Optional.ofNullable((String) jrpcParams.get("autoRenewPeriod"));
        var parsedMemo = Optional.ofNullable((String) jrpcParams.get("memo"));
        var parsedTokenType = Optional.ofNullable((String) jrpcParams.get("tokenType"));
        var parsedSupplyType = Optional.ofNullable((String) jrpcParams.get("supplyType"));
        var parsedMaxSupply = Optional.ofNullable((String) jrpcParams.get("maxSupply"));
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
