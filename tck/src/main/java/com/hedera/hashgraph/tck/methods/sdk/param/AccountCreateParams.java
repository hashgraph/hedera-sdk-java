package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AccountCreateParams for SDK client
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateParams extends JSONRPC2Param {
    // should be optional
    private String key;
    private int initialBalance;
    private boolean receiverSignatureRequired;
    private int autoRenewPeriod;
    private String memo;
    private int maxAutoTokenAssociations;
    private String stakedAccountId;
    private int stakedNodeId;
    private boolean declineStakingReward;
    private String alias;
    private String signerKey;

    @Override
    public AccountCreateParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        String parsedKey = (String) jrpcParams.get("key");
        int parsedInitialBalance = (int) jrpcParams.get("initialBalance");
        boolean parsedReceiverSignatureRequired = (boolean) jrpcParams.get("receiverSignatureRequired");
        int parsedAutoRenewPeriod = (int) jrpcParams.get("autoRenewPeriod");
        String parsedMemo = (String) jrpcParams.get("memo");
        int parsedMaxAutoTokenAssociations = (int) jrpcParams.get("maxAutoTokenAssociations");
        String parsedStakedAccountId = (String) jrpcParams.get("stakedAccountId");
        int parsedStakedNodeId = (int) jrpcParams.get("stakedNodeId");
        boolean parsedDeclineStakingReward = (boolean) jrpcParams.get("declineStakingReward");
        String parsedAlias = (String) jrpcParams.get("alias");
        String parsedSignerKey = (String) jrpcParams.get("signerKey");

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
            parsedSignerKey);
    }
}
