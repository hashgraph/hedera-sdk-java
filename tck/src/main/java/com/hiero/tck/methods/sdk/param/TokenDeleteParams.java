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

import com.hiero.tck.methods.JSONRPC2Param;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONObject;

/**
 * TokenDeleteParams for token delete method
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TokenDeleteParams extends JSONRPC2Param {
    private Optional<String> tokenId;
    private Optional<CommonTransactionParams> commonTransactionParams;

    @Override
    public JSONRPC2Param parse(Map<String, Object> jrpcParams) throws Exception {
        var parsedTokenId = Optional.ofNullable((String) jrpcParams.get("tokenId"));
        Optional<CommonTransactionParams> parsedCommonTransactionParams = Optional.empty();
        if (jrpcParams.containsKey("commonTransactionParams")) {
            JSONObject jsonObject = (JSONObject) jrpcParams.get("commonTransactionParams");
            parsedCommonTransactionParams = Optional.of(CommonTransactionParams.parse(jsonObject));
        }

        return new TokenDeleteParams(parsedTokenId, parsedCommonTransactionParams);
    }
}
