package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptQuery;

/**
 * Get the receipt of a transaction, given its transaction ID.
 *
 * <p>Once a transaction reaches consensus, then information about whether it succeeded or failed
 * will be available until the end of the receipt period.
 *
 * <p>This query is free.
 */
public final class TransactionReceiptQuery
        extends QueryBuilder<TransactionReceipt, TransactionReceiptQuery> {
    private final TransactionGetReceiptQuery.Builder builder;

    public TransactionReceiptQuery() {
        builder = TransactionGetReceiptQuery.newBuilder();
    }

    /**
     * Set the ID of the transaction for which the receipt is being requested.
     *
     * @return {@code this}.
     */
    public TransactionReceiptQuery setTransactionId(TransactionId transactionId) {
        builder.setTransactionID(transactionId.toProtobuf());
        return this;
    }

    @Override
    protected void onBuild(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setTransactionGetReceipt(builder.setHeader(header));
    }

    @Override
    protected TransactionReceipt mapResponse(Response response) {
        return TransactionReceipt.fromProtobuf(response.getTransactionGetReceipt().getReceipt());
    }
}
