package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptQuery;
import io.grpc.MethodDescriptor;

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
    protected boolean isPaymentRequired() {
        return false;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setTransactionGetReceipt(builder.setHeader(header));
    }

    @Override
    protected TransactionReceipt mapResponse(Response response) {
        return TransactionReceipt.fromProtobuf(response.getTransactionGetReceipt().getReceipt());
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getTransactionGetReceipt().getHeader();
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getTransactionGetReceipt().getHeader();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetTransactionReceiptsMethod();
    }

    @Override
    protected boolean shouldRetry(Status status, Response response) {
        if (super.shouldRetry(status, response)) return true;

        var receiptStatus =
                Status.valueOf(response.getTransactionGetReceipt().getReceipt().getStatus());

        switch (receiptStatus) {
            case Busy:
                // node is busy
            case Unknown:
                // still in the node's queue
            case Ok:
                // accepted but has not reached consensus
            case ReceiptNotFound:
                // has reached consensus but not generated
                return true;

            default:
                return false;
        }
    }
}
