package com.hedera.hashgraph.tck.methods.sdk.param;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SetupParamsTest {

    @Test
    void testParse() {
        // Given
        Map<String, Object> jrpcParams = new HashMap<>();
        jrpcParams.put("operatorAccountId", "testAccountId");
        jrpcParams.put("operatorPrivateKey", "testPrivateKey");
        jrpcParams.put("nodeIp", "testNodeIp");
        jrpcParams.put("nodeAccountId", "testNodeAccountId");
        jrpcParams.put("mirrorNetworkIp", "testMirrorNetworkIp");

        // When
        SetupParams result = new SetupParams().parse(jrpcParams);

        // Then
        assertEquals("testAccountId", result.getOperatorAccountId());
        assertEquals("testPrivateKey", result.getOperatorPrivateKey());
        assertEquals("testNodeIp", result.getNodeIp());
        assertEquals("testNodeAccountId", result.getNodeAccountId());
        assertEquals("testMirrorNetworkIp", result.getMirrorNetworkIp());
    }
}
