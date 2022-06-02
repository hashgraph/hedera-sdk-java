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

import java.security.SecureRandom;

/**
 * Internal utility class.
 */
final class ThreadLocalSecureRandom {
    @SuppressWarnings("AnonymousHasLambdaAlternative")
    private static final ThreadLocal<SecureRandom> secureRandom =
        new ThreadLocal<SecureRandom>() {
            @Override
            protected SecureRandom initialValue() {
                return new SecureRandom();
            }
        };

    /**
     * Constructor.
     */
    private ThreadLocalSecureRandom() {
    }

    /**
     * Extract seme randomness.
     *
     * @return                          some randomness
     */
    static SecureRandom current() {
        return secureRandom.get();
    }
}
