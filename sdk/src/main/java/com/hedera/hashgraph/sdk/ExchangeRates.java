package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

/**
 * Contains a set of Exchange Rates (current and next).
 */
public final class ExchangeRates {
    /**
     * Current Exchange Rate
     */
    public final ExchangeRate currentRate;

    /**
     * Next Exchange Rate
     */
    public final ExchangeRate nextRate;

    private ExchangeRates(ExchangeRate currentRate, ExchangeRate nextRate) {
        this.currentRate = currentRate;
        this.nextRate = nextRate;
    }

    static ExchangeRates fromProtobuf(com.hedera.hashgraph.sdk.proto.ExchangeRateSet pb) {
        return new ExchangeRates(
            ExchangeRate.fromProtobuf(pb.getCurrentRate()),
            ExchangeRate.fromProtobuf(pb.getNextRate())
        );
    }

    public static ExchangeRates fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
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
