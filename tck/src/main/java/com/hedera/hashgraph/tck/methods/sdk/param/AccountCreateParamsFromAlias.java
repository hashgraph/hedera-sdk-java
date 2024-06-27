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
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountCreateParamsFromAlias extends JSONRPC2Param {
    private Optional<String> operatorID = Optional.empty();
    private Optional<String> aliasAccountID = Optional.empty();
    private Optional<Long> initialBalance = Optional.empty();

    @Override
    public AccountCreateParamsFromAlias parse(Map<String, Object> jrpcParams) throws ClassCastException {
        Optional<String> operatorID = Optional.ofNullable((String) jrpcParams.get("operator_id"));
        Optional<String> aliasAccountID =
                Optional.ofNullable((String) jrpcParams.get("aliasAccountId")).map(s -> s.replaceAll("\"", ""));
        Optional<Long> initialBalance = Optional.ofNullable((Long) jrpcParams.get("initialBalance"));

        return new AccountCreateParamsFromAlias(operatorID, aliasAccountID, initialBalance);
    }
}
