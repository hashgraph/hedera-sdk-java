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

import com.google.common.base.MoreObjects;

import java.time.Instant;

/**
 * Denotes a conversion between Hbars and cents (USD).
 */
public final class ExchangeRate {
    /**
     * Denotes Hbar equivalent to cents (USD)
     */
    public final int hbars;

    /**
     * Denotes cents (USD) equivalent to Hbar
     */
    public final int cents;

    /**
     * Expiration time of this exchange rate
     */
    public final Instant expirationTime;

    /**
     * Calculated exchange rate
     */
    public final double exchangeRateInCents;

    private ExchangeRate(int hbars, int cents, Instant expirationTime) {
        this.hbars = hbars;
        this.cents = cents;
        this.expirationTime = expirationTime;
        this.exchangeRateInCents = (double) cents / (double) hbars;
    }

    static ExchangeRate fromProtobuf(com.hedera.hashgraph.sdk.proto.ExchangeRate pb) {
        return new ExchangeRate(
            pb.getHbarEquiv(),
            pb.getCentEquiv(),
            InstantConverter.fromProtobuf(pb.getExpirationTime())
        );
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("hbars", hbars)
            .add("cents", cents)
            .add("expirationTime", expirationTime)
            .add("exchangeRateInCents", exchangeRateInCents)
            .toString();
    }
}
