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
 * Common units of hbar; for the most part they follow SI prefix conventions.
 *
 * See <a “https://docs.hedera.com/guides/docs/sdks/hbars#hbar-units”>Hedera Documentation</a>
 */
public enum HbarUnit {
    /**
     * The atomic (smallest) unit of hbar, used natively by the Hedera network.
     * <p>
     * It is equivalent to <sup>1</sup>&frasl;<sub>100,000,000</sub> hbar.
     */
    TINYBAR("tℏ", 1),

    /**
     * Equivalent to 100 tinybar or <sup>1</sup>&frasl;<sub>1,000,000</sub> hbar.
     */
    MICROBAR("μℏ", 100),

    /**
     * Equivalent to 100,000 tinybar or <sup>1</sup>&frasl;<sub>1,000</sub> hbar.
     */
    MILLIBAR("mℏ", 100_000),

    /**
     * The base unit of hbar, equivalent to 100 million tinybar.
     */
    HBAR("ℏ", 100_000_000),

    /**
     * Equivalent to 1 thousand hbar or 100 billion tinybar.
     */
    KILOBAR("kℏ", 1000 * 100_000_000L),

    /**
     * Equivalent to 1 million hbar or 100 trillion tinybar.
     */
    MEGABAR("Mℏ", 1_000_000 * 100_000_000L),

    /**
     * Equivalent to 1 billion hbar or 100 quadillion tinybar.
     * <p>
     * The maximum hbar amount supported by Hedera in any context is ~92 gigabar
     * (2<sup>63</sup> tinybar); use this unit sparingly.
     */
    GIGABAR("Gℏ", 1_000_000_000 * 100_000_000L);

    final long tinybar;

    private final String symbol;

    HbarUnit(String symbol, long tinybar) {
        this.symbol = symbol;
        this.tinybar = tinybar;
    }

    /**
     * Get the preferred symbol of the current unit.
     * <p>
     * E.g. {@link #TINYBAR}.getSymbol() returns "tℏ".
     */
    String getSymbol() {
        return symbol;
    }

    /**
     * Get the name of this unit.
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
