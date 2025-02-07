// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.param.token;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hiero.tck.methods.JSONRPC2Param;
import org.hiero.tck.methods.sdk.param.CommonTransactionParams;
import org.hiero.tck.methods.sdk.param.CustomFee;
import org.hiero.tck.util.JSONRPCParamParser;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenUpdateFeeScheduleParams extends JSONRPC2Param {

    private Optional<String> tokenId;
    private Optional<List<CustomFee>> customFees;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        var parsedTokenId = Optional.ofNullable((String) jrpcParams.get("tokenId"));

        var parsedCommonTransactionParams = JSONRPCParamParser.parseCommonTransactionParams(jrpcParams);

        var parsedCustomFees = JSONRPCParamParser.parseCustomFees(jrpcParams);

        return new TokenUpdateFeeScheduleParams(parsedTokenId, parsedCustomFees, parsedCommonTransactionParams);
    }
}
