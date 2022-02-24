package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Contains a set of Exchange Rates (current and next).
 */
public final class ExchangeRateSet {
    /**
     * Current Exchange Rate
     */
    public final ExchangeRate currentRate;

    /**
     * Next Exchange Rate
     */
    public final ExchangeRate nextRate;

    private ExchangeRateSet(ExchangeRate currentRate, ExchangeRate nextRate) {
        this.currentRate = currentRate;
        this.nextRate = nextRate;
    }

    static ExchangeRateSet fromProtobuf(com.hedera.hashgraph.sdk.proto.ExchangeRateSet pb) {
        return new ExchangeRateSet(
            ExchangeRate.fromProtobuf(pb.getCurrentRate()),
            ExchangeRate.fromProtobuf(pb.getNextRate())
        );
    }

    public static ExchangeRateSet fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.ExchangeRateSet.parseFrom(bytes).toBuilder().build());
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("currentRate", currentRate.toString())
            .add("nextRate", nextRate.toString())
            .toString();
    }

}
