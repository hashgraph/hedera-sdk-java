package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AccountCreateParamsFromAlias for account create method
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateParamsFromAlias extends JSONRPC2Param {
    private Optional<String> operatorID;
    private Optional<String> aliasAccountID;
    private Optional<Long> initialBalance;

    @Override
    public AccountCreateParamsFromAlias parse(Map<String, Object> jrpcParams) throws ClassCastException {
        var operatorID = Optional.ofNullable((String) jrpcParams.get("operator_id"));
        var aliasAccountID =
                Optional.ofNullable((String) jrpcParams.get("aliasAccountId")).map(s -> s.replaceAll("\"", ""));
        var initialBalance = Optional.ofNullable((Long) jrpcParams.get("initialBalance"));

        return new AccountCreateParamsFromAlias(operatorID, aliasAccountID, initialBalance);
    }
}
