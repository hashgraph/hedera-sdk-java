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
package com.hiero.tck.methods;

import java.util.Map;

/**
 * Abstract base class for JSON-RPC parameters. Every parameter POJO should extend this class
 * and implement the {@link #parse} method. This method assists JSON-RPC services in creating
 * parameters for their JSON-RPC methods from the request.
 *
 * IMPORTANT:
 * all inheriting classes should include the following Lombok annotations:
 * {@code @Getter}, {@code @AllArgsConstructor}, and {@code @NoArgsConstructor}.
 * These annotations are needed for the instance creation via reflection.
 *
 */
public abstract class JSONRPC2Param {
    public abstract JSONRPC2Param parse(final Map<String, Object> jrpcParams) throws Exception;
}
