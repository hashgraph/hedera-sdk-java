/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
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
package com.hedera.hashgraph.sdk;

/**
 * Signals that a key could not be realized from the given input.
 * <p>
 * This exception can be raised by any of the {@code from} methods
 * on {@link PrivateKey} or {@link PublicKey}.
 */
public final class BadKeyException extends IllegalArgumentException {
    /**
     * @param message                   the message
     */
    BadKeyException(String message) {
        super(message);
    }

    /**
     * @param cause                     the cause
     */
    BadKeyException(Throwable cause) {
        super(cause);
    }
}
