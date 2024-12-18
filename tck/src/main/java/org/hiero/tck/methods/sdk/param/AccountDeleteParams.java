// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.param;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;
import org.hiero.tck.methods.JSONRPC2Param;

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
