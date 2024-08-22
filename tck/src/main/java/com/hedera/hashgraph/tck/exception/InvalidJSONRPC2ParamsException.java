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
package com.hedera.hashgraph.tck.exception;

/**
 * Thrown when the server cannot parse the given parameters.
 * This error should be thrown from the param parser
 */
public class InvalidJSONRPC2ParamsException extends Exception {

    public InvalidJSONRPC2ParamsException() {
        super();
    }

    public InvalidJSONRPC2ParamsException(String message) {
        super(message);
    }

    public InvalidJSONRPC2ParamsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidJSONRPC2ParamsException(Throwable cause) {
        super(cause);
    }
}
