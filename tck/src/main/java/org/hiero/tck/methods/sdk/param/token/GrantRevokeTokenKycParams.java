// SPDX-License-Identifier: Apache-2.0
package org.hiero.tck.methods.sdk.param.token;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hiero.tck.exception.JSONRPCParseException;
import org.hiero.tck.methods.JSONRPC2Param;
import org.hiero.tck.methods.sdk.param.CommonTransactionParams;
import org.hiero.tck.util.JSONRPCParamParser;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GrantRevokeTokenKycParams extends JSONRPC2Param {
    private Optional<String> tokenId;
    private Optional<String> accountId;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        try {
            var parsedTokenId = Optional.ofNullable((String) jrpcParams.get("tokenId"));
            var parsedAccountId = Optional.ofNullable((String) jrpcParams.get("accountId"));
            var parsedCommonTransactionParams = JSONRPCParamParser.parseCommonTransactionParams(jrpcParams);

            return new GrantRevokeTokenKycParams(parsedTokenId, parsedAccountId, parsedCommonTransactionParams);
        } catch (ClassCastException e) {
            throw new JSONRPCParseException("Invalid parameter type", e);
        } catch (JSONRPCParseException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONRPCParseException("Failed to parse GrantRevokeTokenKyc parameters", e);
        }
    }
}
