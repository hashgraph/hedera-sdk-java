package org.hiero.tck.methods.sdk.param.token;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hiero.tck.exception.JSONRPCParseException;
import org.hiero.tck.methods.JSONRPC2Param;
import org.hiero.tck.methods.sdk.param.CommonTransactionParams;
import org.hiero.tck.util.JSONRPCParamParser;

import java.util.Map;
import java.util.Optional;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PauseUnpauseTokenParams extends JSONRPC2Param {
    private Optional<String> tokenId;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        try {
            var parsedTokenId = Optional.ofNullable((String) jrpcParams.get("tokenId"));
            var parsedCommonTransactionParams =
                JSONRPCParamParser.parseCommonTransactionParams(jrpcParams);

            return new PauseUnpauseTokenParams(
                parsedTokenId,
                parsedCommonTransactionParams);
        } catch (ClassCastException e) {
            throw new JSONRPCParseException("Invalid parameter type", e);
        } catch (JSONRPCParseException e) {
            throw e;
        } catch (Exception e) {
            throw new JSONRPCParseException("Failed to parse PauseUnpauseToken parameters", e);
        }
    }
}
