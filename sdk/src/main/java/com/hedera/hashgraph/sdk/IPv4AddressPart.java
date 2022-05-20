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
 * Simplified representation of the 16 bit half of an IPv4Address.
 */
public class IPv4AddressPart {
    /**
     * Represents the first byte.
     */
    byte left;
    /**
     * Represents the last byte.
     */
    byte right;

    /**
     * Constructor.
     */
    IPv4AddressPart() {
    }

    /**
     * @return                          the first byte
     */
    public byte getLeft() {
        return left;
    }

    /**
     * Assign the first byte.
     *
     * @param left                      the first byte
     * @return {@code this}
     */
    public IPv4AddressPart setLeft(byte left) {
        this.left = left;
        return this;
    }

    /**
     * @return                          the last byte
     */
    public byte getRight() {
        return right;
    }

    /**
     * Assign the last byte.
     *
     * @param right                     the last byte
     * @return {@code this}
     */
    public IPv4AddressPart setRight(byte right) {
        this.right = right;
        return this;
    }

    @Override
    public String toString() {
        return ((int) left & 0x000000FF) + "." + ((int) right & 0x000000FF);
    }
}
