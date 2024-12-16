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

import com.hiero.sdk.Status;

/**
 * Custom JSON-RPC error definitions
 */
public class JSONRPC2Error {
    private JSONRPC2Error() {
        // static utility class
    }

    public static final int HEDERA_STATUS_CODE = -32001;
    public static final com.thetransactioncompany.jsonrpc2.JSONRPC2Error HEDERA_ERROR =
            new com.thetransactioncompany.jsonrpc2.JSONRPC2Error(HEDERA_STATUS_CODE, "Hedera error");

    public record ErrorData(Status status, String message) {}
}
