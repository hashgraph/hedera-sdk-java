package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

/**
 * AccountDeleteParams for account delete method
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AccountDeleteParams extends JSONRPC2Param {
    private Optional<String> deleteAccountId;
    private Optional<String> transferAccountId;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public AccountDeleteParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        var parsedDeleteAccountId = Optional.ofNullable((String) jrpcParams.get("deleteAccountId"));
        var parsedTransferAccountId = Optional.ofNullable((String) jrpcParams.get("transferAccountId"));

        Optional<CommonTransactionParams> parsedCommonTransactionParams = Optional.empty();
        if (jrpcParams.containsKey("commonTransactionParams")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("commonTransactionParams");
            parsedCommonTransactionParams = Optional.of(CommonTransactionParams.parse(jsonObject));
        }

        return new AccountDeleteParams(parsedDeleteAccountId, parsedTransferAccountId, parsedCommonTransactionParams);
    }
}
