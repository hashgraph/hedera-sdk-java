/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2021 - 2024 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.sdk.test.integration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NftMetadataGenerator {
    private NftMetadataGenerator() {
    }

    public static List<byte[]> generate(byte metadataCount) {
        List<byte[]> metadatas = new ArrayList<>();
        for (byte i = 0; i < metadataCount; i++) {
            byte[] md = {i};
            metadatas.add(md);
        }
        return metadatas;
    }

    public static List<byte[]> generate(byte[] metadata, int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> metadata.clone())
            .collect(Collectors.toList());
    }

    public static List<byte[]> generateOneLarge() {
        return Collections.singletonList(new byte[101]);
    }
}
