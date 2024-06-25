package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
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
    private String operatorID;
    private String aliasAccountID;
    private int initialBalance;

    @Override
    public AccountCreateParamsFromAlias parse(Map<String, Object> jrpcParams) throws ClassCastException {
        String operatorID = (String) jrpcParams.get("operator_id");
        String aliasAccountID = (String) jrpcParams.get("aliasAccountId");
        int initialBalance = (Integer) jrpcParams.get("initialBalance");
        return new AccountCreateParamsFromAlias(operatorID, aliasAccountID, initialBalance);
    }
}
