package com.hedera.hashgraph.tck.methods.sdk.param;

import com.hedera.hashgraph.tck.methods.JSONRPC2Param;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GeneratePublicKeyParams extends JSONRPC2Param {
    private String privateKey;

    @Override
    public GeneratePublicKeyParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        String privateKey = (String) jrpcParams.get("privateKey");
        return new GeneratePublicKeyParams(privateKey);
    }
}
