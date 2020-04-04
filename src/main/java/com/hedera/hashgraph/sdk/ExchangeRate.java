package com.hedera.hashgraph.sdk;

import org.threeten.bp.Instant;

/** Denotes a conversion between Hbars and cents (USD). */
public final class ExchangeRate {
    /** Denotes Hbar equivalent to cents (USD) */
    public final int hbars;

    /** Denotes cents (USD) equivalent to Hbar */
    public final int cents;

    /** Expiration time of this exchange rate */
    public final Instant expirationTime;

    private ExchangeRate(int hbars, int cents, Instant expirationTime) {
        this.hbars = hbars;
        this.cents = cents;
        this.expirationTime = expirationTime;
    }

    static ExchangeRate fromProtobuf(com.hedera.hashgraph.sdk.proto.ExchangeRate pb) {
        return new ExchangeRate(
                pb.getHbarEquiv(),
                pb.getCentEquiv(),
                InstantConverter.fromProtobuf(pb.getExpirationTime()));
    }

    @Override
    public String toString() {
        return "ExchangeRate{"
                + "hbars="
                + hbars
                + ", cents="
                + cents
                + ", expirationTime="
                + expirationTime
                + '}';
    }
}
