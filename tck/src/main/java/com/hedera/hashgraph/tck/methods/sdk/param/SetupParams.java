package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SetupParams for SDK client
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SetupParams extends JSONRPC2Param {
    private String operatorAccountId;
    private String operatorPrivateKey;
    private String nodeIp;
    private String nodeAccountId;
    private String mirrorNetworkIp;

    @Override
    public SetupParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        String parsedOperatorAccountId = (String) jrpcParams.get("operatorAccountId");
        String parsedOperatorPrivateKey = (String) jrpcParams.get("operatorPrivateKey");
        String parsedNodeIp = (String) jrpcParams.get("nodeIp");
        String parsedNodeAccountId = (String) jrpcParams.get("nodeAccountId");
        String parsedMirrorNetworkIp = (String) jrpcParams.get("mirrorNetworkIp");

        return new SetupParams(
                parsedOperatorAccountId,
                parsedOperatorPrivateKey,
                parsedNodeIp,
                parsedNodeAccountId,
                parsedMirrorNetworkIp);
    }
}
