package com.hedera.hashgraph.sdk;

public final class MaxQueryPaymentExceededException extends RuntimeException {
    /**
     * The cost of the query that was attempted as returned by {@link QueryBuilder#getCost(Client)}.
     */
    public final Hbar queryCost;

    /**
     * The limit for a single automatic query payment, set by
     * {@link Client#setMaxQueryPayment(Hbar)} or {@link QueryBuilder#setMaxQueryPayment(Hbar)}.
     */
    public final Hbar maxQueryPayment;

    MaxQueryPaymentExceededException(QueryBuilder<?, ?> builder, Hbar cost, Hbar maxQueryPayment) {
        super(String.format(
            "cost of %s (%s) without explicit payment is greater than "
                + "Client.maxQueryPayment (%s)",
            builder.getClass().getSimpleName(),
            cost,
            maxQueryPayment));

        this.queryCost = cost;
        this.maxQueryPayment = maxQueryPayment;
    }
}
