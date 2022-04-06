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
