// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.param.account;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hiero.tck.methods.JSONRPC2Param;
import org.hiero.tck.methods.sdk.param.CommonTransactionParams;
import org.hiero.tck.util.JSONRPCParamParser;

/**
 * AccountUpdateParams for account update method
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountUpdateParams extends JSONRPC2Param {
    private Optional<String> key;
    private Optional<Boolean> receiverSignatureRequired;
    private Optional<Long> autoRenewPeriod;
    private Optional<String> memo;
    private Optional<Long> expirationTime;
    private Optional<Long> maxAutoTokenAssociations;
    private Optional<String> stakedAccountId;
    private Optional<String> accountId;
    private Optional<Long> stakedNodeId;
    private Optional<Boolean> declineStakingReward;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public AccountUpdateParams parse(Map<String, Object> jrpcParams) throws Exception {
        var parsedKey = Optional.ofNullable((String) jrpcParams.get("key"));
        var parsedReceiverSignatureRequired =
                Optional.ofNullable((Boolean) jrpcParams.get("receiverSignatureRequired"));
        var parsedAutoRenewPeriod = Optional.ofNullable((Long) jrpcParams.get("autoRenewPeriod"));
        var parsedMemo = Optional.ofNullable((String) jrpcParams.get("memo"));
        var parsedMaxAutoTokenAssociations = Optional.ofNullable((Long) jrpcParams.get("maxAutoTokenAssociations"));
        var parsedStakedAccountId = Optional.ofNullable((String) jrpcParams.get("stakedAccountId"));
        var parsedAccountId = Optional.ofNullable((String) jrpcParams.get("accountId"));
        var parsedStakedNodeId = Optional.ofNullable((Long) jrpcParams.get("stakedNodeId"));
        var parsedExpirationTime = Optional.ofNullable((Long) jrpcParams.get("expirationTime"));
        var parsedDeclineStakingReward = Optional.ofNullable((Boolean) jrpcParams.get("declineStakingReward"));
        var parsedCommonTransactionParams = JSONRPCParamParser.parseCommonTransactionParams(jrpcParams);

        return new AccountUpdateParams(
                parsedKey,
                parsedReceiverSignatureRequired,
                parsedAutoRenewPeriod,
                parsedMemo,
                parsedExpirationTime,
                parsedMaxAutoTokenAssociations,
                parsedStakedAccountId,
                parsedAccountId,
                parsedStakedNodeId,
                parsedDeclineStakingReward,
                parsedCommonTransactionParams);
    }
}
