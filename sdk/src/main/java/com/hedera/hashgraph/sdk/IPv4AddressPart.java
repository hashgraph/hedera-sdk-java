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

public class IPv4AddressPart {
    byte left;
    byte right;

    IPv4AddressPart() {
    }

    public byte getLeft() {
        return left;
    }

    public IPv4AddressPart setLeft(byte left) {
        this.left = left;
        return this;
    }

    public byte getRight() {
        return right;
    }

    public IPv4AddressPart setRight(byte right) {
        this.right = right;
        return this;
    }

    @Override
    public String toString() {
        return ((int) left & 0x000000FF) + "." + ((int) right & 0x000000FF);
    }
}
