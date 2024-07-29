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
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SetupParams extends JSONRPC2Param {
    private String operatorAccountId;
    private String operatorPrivateKey;
    private Optional<String> nodeIp;
    private Optional<String> nodeAccountId;
    private Optional<String> mirrorNetworkIp;

    @Override
    public SetupParams parse(Map<String, Object> jrpcParams) throws ClassCastException {
        var parsedOperatorAccountId = (String) jrpcParams.get("operatorAccountId");
        var parsedOperatorPrivateKey = (String) jrpcParams.get("operatorPrivateKey");
        var parsedNodeIp = Optional.ofNullable((String) jrpcParams.get("nodeIp"));
        var parsedNodeAccountId = Optional.ofNullable((String) jrpcParams.get("nodeAccountId"));
        var parsedMirrorNetworkIp = Optional.ofNullable((String) jrpcParams.get("mirrorNetworkIp"));

        return new SetupParams(
                parsedOperatorAccountId,
                parsedOperatorPrivateKey,
                parsedNodeIp,
                parsedNodeAccountId,
                parsedMirrorNetworkIp);
    }
}
