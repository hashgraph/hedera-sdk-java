package com.hedera.hashgraph.tck.methods.sdk;

import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Method;
import com.hedera.hashgraph.tck.annotation.JSONRPC2Service;
import com.hedera.hashgraph.tck.methods.AbstractJSONRPC2Service;
import com.hedera.hashgraph.tck.methods.sdk.param.GeneratePublicKeyParams;

@JSONRPC2Service
public class KeyService extends AbstractJSONRPC2Service {
    @JSONRPC2Method("generatePublicKey")
    public String generatePublicKey(final GeneratePublicKeyParams params) {
        var trimmedKey = params.getPrivateKey().trim();
        var key = PrivateKey.fromString(trimmedKey);

        return key.getPublicKey().toString();
    }

    @JSONRPC2Method("generatePrivateKey")
    public String generatePrivateKey() {
        var key = PrivateKey.generateED25519();
        return key.toString();
    }
}
