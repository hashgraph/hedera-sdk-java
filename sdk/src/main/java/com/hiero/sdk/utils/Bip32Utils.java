/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2023 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk.utils;

/**
 * Utility class for BIP32 functionalities
 */
public class Bip32Utils {

    /**
     * Indicates if the index is hardened
     */
    public static final int HARDENED_BIT = 0x80000000;

    private Bip32Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Harden the index
     *
     * @param index         the derivation index
     * @return              the hardened index
     */
    public static int toHardenedIndex(int index) {
        return index | HARDENED_BIT;
    }

    /**
     * Check if the index is hardened
     *
     * @param index         the derivation index
     * @return              true if the index is hardened
     */
    public static boolean isHardenedIndex(int index) {
        return (index & HARDENED_BIT) != 0;
    }
}
