package com.hedera.hashgraph.sdk;

import com.esaulpaugh.headlong.rlp.RLPDecoder;

public abstract class EthereumTransactionData {
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
     */
    public abstract byte[] toBytes();

    /**
     * Serialize the ethereum transaction data into a string
     */
    public abstract String toString();
}
