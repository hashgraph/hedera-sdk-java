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
 * Enum for the network names.
 */
@Deprecated
public enum NetworkName {
    /**
     * The mainnet network
     */
    @Deprecated
    MAINNET(0),
    /**
     * The testnet network
     */
    @Deprecated
    TESTNET(1),
    /**
     * The previewnet network
     */
    @Deprecated
    PREVIEWNET(2),
    /**
     * Other network
     */
    @Deprecated
    OTHER(Integer.MAX_VALUE);

    final int id;

    NetworkName(int id) {
        this.id = id;
    }

    /**
     * Assign the network name via a string name.
     *
     * @param networkName               the string containing the network name
     * @return                          the ledger id
     */
    public static NetworkName fromString(String networkName) {
        switch (networkName) {
            case "mainnet":
                return NetworkName.MAINNET;
            case "testnet":
                return NetworkName.TESTNET;
            case "previewnet":
                return NetworkName.PREVIEWNET;
            default:
                throw new IllegalArgumentException("The only supported network names are 'mainnet', 'testnet', and 'previewnet'");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case MAINNET:
                return "mainnet";
            case TESTNET:
                return "testnet";
            case PREVIEWNET:
                return "previewnet";
            default:
                throw new IllegalStateException("(BUG) `NetworkName.toString()` switch is non-exhaustive");
        }
    }
}
