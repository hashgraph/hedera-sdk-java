/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hiero.tck.methods.sdk.param;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
        assertEquals(Optional.of("testNodeIp"), result.getNodeIp());
        assertEquals(Optional.of("testNodeAccountId"), result.getNodeAccountId());
        assertEquals(Optional.of("testMirrorNetworkIp"), result.getMirrorNetworkIp());
    }
}
