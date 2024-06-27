package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import java.util.Optional;
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
    private Optional<String> operatorAccountId = Optional.empty();
    private Optional<String> operatorPrivateKey = Optional.empty();
    private Optional<String> nodeIp = Optional.empty();
    private Optional<String> nodeAccountId = Optional.empty();
    private Optional<String> mirrorNetworkIp = Optional.empty();

    @Override
    public SetupParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        Optional<String> parsedOperatorAccountId = Optional.ofNullable((String) jrpcParams.get("operatorAccountId"));
        Optional<String> parsedOperatorPrivateKey = Optional.ofNullable((String) jrpcParams.get("operatorPrivateKey"));
        Optional<String> parsedNodeIp = Optional.ofNullable((String) jrpcParams.get("nodeIp"));
        Optional<String> parsedNodeAccountId = Optional.ofNullable((String) jrpcParams.get("nodeAccountId"));
        Optional<String> parsedMirrorNetworkIp = Optional.ofNullable((String) jrpcParams.get("mirrorNetworkIp"));

        return new SetupParams(
                parsedOperatorAccountId,
                parsedOperatorPrivateKey,
                parsedNodeIp,
                parsedNodeAccountId,
                parsedMirrorNetworkIp);
    }
}
