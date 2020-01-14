package com.hedera.hashgraph.sdk;

public final class MaxQueryPaymentExceededException extends RuntimeException implements HederaThrowable {
    /**
     * The cost of the query that was attempted as returned by {@link QueryBuilder#getCost(Client)}.
     */
    public final Hbar queryCost;

    /**
     * The limit for a single automatic query payment, set by
     * {@link Client#setMaxQueryPayment(Hbar)} or {@link QueryBuilder#setMaxQueryPayment(Hbar)}.
     */
    public final Hbar maxQueryPayment;

    MaxQueryPaymentExceededException(QueryBuilder<?, ?> builder, long cost, long maxQueryPayment) {
        super(String.format(
            "cost of %s (%d) without explicit payment is greater than "
                + "Client.maxQueryPayment (%d)",
            builder.getClass().getSimpleName(),
            cost,
            maxQueryPayment));

        this.queryCost = Hbar.fromTinybar(cost);
        this.maxQueryPayment = Hbar.fromTinybar(maxQueryPayment);
    }
}
