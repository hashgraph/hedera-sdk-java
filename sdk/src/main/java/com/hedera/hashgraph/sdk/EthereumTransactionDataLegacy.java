package com.hedera.hashgraph.sdk;

import com.esaulpaugh.headlong.rlp.RLPDecoder;
import com.esaulpaugh.headlong.rlp.RLPEncoder;
import com.esaulpaugh.headlong.rlp.RLPItem;
import com.google.common.base.MoreObjects;
import org.bouncycastle.util.encoders.Hex;

import java.math.BigInteger;
import java.util.List;

/**
 * The ethereum transaction data, in the legacy format
 */
public class EthereumTransactionDataLegacy extends EthereumTransactionData {
    public byte[] chainId = new byte[]{};
    public byte[] nonce;
    public byte[] gasPrice;
    public byte[] gasLimit;
    public byte[] to;
    public byte[] value;
    public byte[] v;
    public int recoveryId;
    public byte[] r;
    public byte[] s;

    EthereumTransactionDataLegacy(
        byte[] nonce,
        byte[] gasPrice,
        byte[] gasLimit,
        byte[] to,
        byte[] value,
        byte[] callData,
        byte[] v,
        byte[] r,
        byte[] s
    ) {
        super(callData);

        this.nonce = nonce;
        this.gasPrice = gasPrice;
        this.gasLimit = gasLimit;
        this.to = to;
        this.value = value;
        this.v = v;
        this.r = r;
        this.s = s;

        var vBI = new BigInteger(1, this.v);
        this.recoveryId = vBI.testBit(0) ? 0 : 1;

        if (vBI.compareTo(BigInteger.valueOf(34)) > 0) {
            this.chainId = vBI.subtract(BigInteger.valueOf(35)).shiftRight(1).toByteArray();
        }
    }

    public static EthereumTransactionDataLegacy fromBytes(byte[] bytes) {
        var decoder = RLPDecoder.RLP_STRICT.sequenceIterator(bytes);
        var rlpItem = decoder.next();

        List<RLPItem> rlpList = rlpItem.asRLPList().elements();
        if (rlpList.size() != 9) {
            throw new IllegalArgumentException("expected 9 RLP encoded elements, found " + rlpList.size());
        }

        return new EthereumTransactionDataLegacy(
            rlpList.get(0).data(),
            rlpList.get(1).asBytes(),
            rlpList.get(2).data(),
            rlpList.get(3).data(),
            rlpList.get(4).data(),
            rlpList.get(5).data(),
            rlpList.get(6).asBytes(),
            rlpList.get(7).data(),
            rlpList.get(8).data()
        );
    }

    public byte[] toBytes() {
        return RLPEncoder.encodeAsList(nonce, gasPrice, gasLimit, to, value, callData, v, r, s);
    }

    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("chainId", Hex.toHexString(this.chainId))
            .add("nonce", Hex.toHexString(this.nonce))
            .add("gasPrice", Hex.toHexString(this.gasPrice))
            .add("gasLimit", Hex.toHexString(this.gasLimit))
            .add("to", Hex.toHexString(this.to))
            .add("value", Hex.toHexString(this.value))
            .add("recoveryId", this.recoveryId)
            .add("v", Hex.toHexString(this.v))
            .add("r", Hex.toHexString(this.r))
            .add("s", Hex.toHexString(this.s))
            .toString();
    }
}
