package org.hiero.tck.methods.sdk.param.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hiero.tck.exception.JSONRPCParseException;
import org.hiero.tck.methods.JSONRPC2Param;
import org.hiero.tck.methods.sdk.param.CommonTransactionParams;
import org.hiero.tck.util.JSONRPCParamParser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AssociateDisassociateTokenParams extends JSONRPC2Param {
    private Optional<String> accountId;
    private Optional<List<String>> tokenIds;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        try {
            var parsedAccountId = Optional.ofNullable((String) jrpcParams.get("accountId"));
            var parsedTokenIds = Optional.ofNullable((List<String>) jrpcParams.get("tokenIds"));
            var parsedCommonTransactionParams =
                JSONRPCParamParser.parseCommonTransactionParams(jrpcParams);

            return new AssociateDisassociateTokenParams(
                parsedAccountId,
                parsedTokenIds,
                parsedCommonTransactionParams);
        } catch (ClassCastException e) {
            throw new JSONRPCParseException("Invalid parameter type", e);
        } catch (JSONRPCParseException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONRPCParseException("Failed to parse AssociateDisassociateToken parameters", e);
        }
    }
}
