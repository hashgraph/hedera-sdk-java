package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AccountCreateParams for account create method
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateParams extends JSONRPC2Param {
    private Optional<String> publicKey;
    private Optional<String> privateKey;
    private Optional<Long> initialBalance;
    private Optional<Boolean> receiverSignatureRequired;
    private Optional<String> autoRenewPeriod;
    private Optional<String> memo;
    private Optional<Long> maxAutoTokenAssociations;
    private Optional<String> stakedAccountId;
    private Optional<Long> stakedNodeId;
    private Optional<Boolean> declineStakingReward;
    private Optional<String> alias;
    private Optional<String> signerKey;

    @Override
    public AccountCreateParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        Optional<String> publicKey = Optional.ofNullable((String) jrpcParams.get("publicKey"));
        Optional<String> privateKey = Optional.ofNullable((String) jrpcParams.get("privateKey"));
        Optional<Long> parsedInitialBalance = Optional.ofNullable((Long) jrpcParams.get("initialBalance"));
        Optional<Boolean> parsedReceiverSignatureRequired =
                Optional.ofNullable((Boolean) jrpcParams.get("receiverSignatureRequired"));
        Optional<String> parsedAutoRenewPeriod = Optional.ofNullable((String) jrpcParams.get("autoRenewPeriod"));
        Optional<String> parsedMemo = Optional.ofNullable((String) jrpcParams.get("accountMemo"));
        Optional<Long> parsedMaxAutoTokenAssociations =
                Optional.ofNullable((Long) jrpcParams.get("maxAutomaticTokenAssociations"));
        Optional<String> parsedStakedAccountId = Optional.ofNullable((String) jrpcParams.get("stakedAccountId"));
        Optional<Long> parsedStakedNodeId = Optional.ofNullable((Long) jrpcParams.get("stakedNodeId"));
        Optional<Boolean> parsedDeclineStakingReward =
                Optional.ofNullable((Boolean) jrpcParams.get("declineStakingReward"));
        Optional<String> parsedAlias = Optional.ofNullable((String) jrpcParams.get("aliasAccountId"));
        Optional<String> parsedSignerKey = Optional.ofNullable((String) jrpcParams.get("signerKey"));

        return new AccountCreateParams(
                publicKey,
                privateKey,
                parsedInitialBalance,
                parsedReceiverSignatureRequired,
                parsedAutoRenewPeriod,
                parsedMemo,
                parsedMaxAutoTokenAssociations,
                parsedStakedAccountId,
                parsedStakedNodeId,
                parsedDeclineStakingReward,
                parsedAlias,
                parsedSignerKey);
    }
}
