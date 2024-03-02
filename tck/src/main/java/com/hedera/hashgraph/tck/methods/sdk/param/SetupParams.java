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
        String operatorAccountId = (String) jrpcParams.get("operatorAccountId");
        String operatorPrivateKey = (String) jrpcParams.get("operatorPrivateKey");
        String nodeIp = (String) jrpcParams.get("nodeIp");
        String nodeAccountId = (String) jrpcParams.get("nodeAccountId");
        String mirrorNetworkIp = (String) jrpcParams.get("mirrorNetworkIp");

        return new SetupParams(operatorAccountId, operatorPrivateKey, nodeIp, nodeAccountId, mirrorNetworkIp);
    }
}
