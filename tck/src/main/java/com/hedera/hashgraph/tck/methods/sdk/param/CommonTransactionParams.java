package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CommonTransactionParams (part of AccountCreateParams) for SDK client
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommonTransactionParams extends JSONRPC2Param {
    private Optional<Long> maxTransactionFee;
    private Optional<Long> validTransactionDuration;
    private Optional<String> memo;
    private Optional<Boolean> regenerateTransactionId;
    private Optional<List<String>> signers;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws ClassCastException {
        Optional<Long> maxTransactionFee = Optional.ofNullable((Long) jrpcParams.get("maxTransactionFee"));
        Optional<Long> validTransactionDuration =
                Optional.ofNullable((Long) jrpcParams.get("validTransactionDuration"));
        Optional<String> memo = Optional.ofNullable((String) jrpcParams.get("memo"));
        Optional<Boolean> regenerateTransactionId =
                Optional.ofNullable((Boolean) jrpcParams.get("regenerateTransactionId"));

        // TODO: double check it
        Optional<List<String>> signers = Optional.ofNullable((List<String>) jrpcParams.get("signers"));

        return new CommonTransactionParams(
                maxTransactionFee, validTransactionDuration, memo, regenerateTransactionId, signers);
    }
}
