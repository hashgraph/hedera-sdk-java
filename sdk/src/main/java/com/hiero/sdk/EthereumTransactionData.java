/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2022 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk;

import com.esaulpaugh.headlong.rlp.RLPDecoder;

/**
 * This class represents the data of an Ethereum transaction.
 * <p>
 * It may be of subclass {@link EthereumTransactionDataLegacy} or of subclass {@link EthereumTransactionDataEip1559}
 */
public abstract class EthereumTransactionData {
    /**
     * The raw call data.
     */
    public byte[] callData;

    EthereumTransactionData(byte[] callData) {
        this.callData = callData;
    }

    static EthereumTransactionData fromBytes(byte[] bytes) {
        var decoder = RLPDecoder.RLP_STRICT.sequenceIterator(bytes);
        var rlpItem = decoder.next();
        if (rlpItem.isList()) {
            return EthereumTransactionDataLegacy.fromBytes(bytes);
        } else {
            return EthereumTransactionDataEip1559.fromBytes(bytes);
        }
    }

    /**
     * Serialize the ethereum transaction data into bytes using RLP
     *
     * @return the serialized transaction as a byte array
     */
    public abstract byte[] toBytes();

    /**
     * Serialize the ethereum transaction data into a string
     *
     * @return the serialized transaction as a string
     */
    public abstract String toString();
}
