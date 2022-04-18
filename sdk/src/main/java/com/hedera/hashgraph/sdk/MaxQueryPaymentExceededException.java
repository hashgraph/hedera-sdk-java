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
 * Signals that a query will cost more than a pre-configured maximum payment amount.
 */
public final class MaxQueryPaymentExceededException extends RuntimeException {
    /**
     * The cost of the query that was attempted as returned by {@link Query#getCost(Client)}.
     */
    public final Hbar queryCost;

    /**
     * The limit for a single automatic query payment, set by
     * {@link Client#setMaxQueryPayment(Hbar)} or {@link Query#setMaxQueryPayment(Hbar)}.
     */
    public final Hbar maxQueryPayment;

    MaxQueryPaymentExceededException(Query<?, ?> builder, Hbar cost, Hbar maxQueryPayment) {
        super(String.format(
            "cost for %s, of %s, without explicit payment is greater than "
                + "the maximum allowed payment of %s",
            builder.getClass().getSimpleName(),
            cost,
            maxQueryPayment));

        this.queryCost = cost;
        this.maxQueryPayment = maxQueryPayment;
    }
}
