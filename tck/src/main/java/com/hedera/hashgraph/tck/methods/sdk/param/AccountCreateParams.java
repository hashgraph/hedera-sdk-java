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
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

/**
 * AccountCreateParams for account create method
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateParams extends JSONRPC2Param {
    private Optional<String> key;
    private Optional<Long> initialBalance;
    private Optional<Boolean> receiverSignatureRequired;
    private Optional<Long> autoRenewPeriod;
    private Optional<String> memo;
    private Optional<Long> maxAutoTokenAssociations;
    private Optional<String> stakedAccountId;
    private Optional<Long> stakedNodeId;
    private Optional<Boolean> declineStakingReward;
    private Optional<String> alias;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public AccountCreateParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        var parsedKey = Optional.ofNullable((String) jrpcParams.get("key"));
        var parsedInitialBalance = Optional.ofNullable((Long) jrpcParams.get("initialBalance"));
        var parsedReceiverSignatureRequired =
                Optional.ofNullable((Boolean) jrpcParams.get("receiverSignatureRequired"));
        var parsedAutoRenewPeriod = Optional.ofNullable((Long) jrpcParams.get("autoRenewPeriod"));
        var parsedMemo = Optional.ofNullable((String) jrpcParams.get("memo"));
        var parsedMaxAutoTokenAssociations = Optional.ofNullable((Long) jrpcParams.get("maxAutoTokenAssociations"));
        var parsedStakedAccountId = Optional.ofNullable((String) jrpcParams.get("stakedAccountId"));
        var parsedStakedNodeId = Optional.ofNullable((Long) jrpcParams.get("stakedNodeId"));
        var parsedDeclineStakingReward = Optional.ofNullable((Boolean) jrpcParams.get("declineStakingReward"));
        var parsedAlias = Optional.ofNullable((String) jrpcParams.get("alias"));
        Optional<CommonTransactionParams> parsedCommonTransactionParams = Optional.empty();
        if (jrpcParams.containsKey("commonTransactionParams")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("commonTransactionParams");
            parsedCommonTransactionParams = Optional.of(CommonTransactionParams.parse(jsonObject));
        }

        return new AccountCreateParams(
                parsedKey,
                parsedInitialBalance,
                parsedReceiverSignatureRequired,
                parsedAutoRenewPeriod,
                parsedMemo,
                parsedMaxAutoTokenAssociations,
                parsedStakedAccountId,
                parsedStakedNodeId,
                parsedDeclineStakingReward,
                parsedAlias,
                parsedCommonTransactionParams);
    }
}
