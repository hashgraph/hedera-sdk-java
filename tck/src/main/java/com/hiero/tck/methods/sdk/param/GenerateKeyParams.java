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
import com.hiero.tck.util.KeyUtils.KeyType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class GenerateKeyParams extends JSONRPC2Param {
    private KeyType type;
    private Optional<String> fromKey;
    private Optional<Long> threshold;
    private Optional<List<GenerateKeyParams>> keys;

    @Override
    public GenerateKeyParams parse(Map<String, Object> jrpcParams) throws Exception {
        var parsedType = (String) jrpcParams.get("type");
        var parsedFromKey = Optional.ofNullable((String) jrpcParams.get("fromKey"));
        var parsedThreshold = Optional.ofNullable((Long) jrpcParams.get("threshold"));

        Optional<List<GenerateKeyParams>> parsedKeys = Optional.empty();
        if (jrpcParams.containsKey("keys")) {
            JSONArray jsonArray = (JSONArray) jrpcParams.get("keys");
            List<GenerateKeyParams> keyList = new ArrayList<>();
            for (Object o : jsonArray) {
                JSONObject jsonObject = (JSONObject) o;
                GenerateKeyParams keyParam = new GenerateKeyParams().parse(jsonObject);
                keyList.add(keyParam);
            }
            parsedKeys = Optional.of(keyList);
        }

        return new GenerateKeyParams(KeyType.fromString(parsedType), parsedFromKey, parsedThreshold, parsedKeys);
    }
}
