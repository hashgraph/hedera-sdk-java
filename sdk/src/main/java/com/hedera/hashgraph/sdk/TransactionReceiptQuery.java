package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
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
        extends Query<TransactionReceipt, TransactionReceiptQuery> {
    private final TransactionGetReceiptQuery.Builder builder;

    public TransactionReceiptQuery() {
        builder = TransactionGetReceiptQuery.newBuilder();
    }

    /**
     * Set the ID of the transaction for which the receipt is being requested.
     *
     * @return {@code this}
     * @param transactionId The TransactionId to be set
     */
    public TransactionReceiptQuery setTransactionId(TransactionId transactionId) {
        builder.setTransactionID(transactionId.toProtobuf());
        return this;
    }

    @Override
    boolean isPaymentRequired() {
        return false;
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setTransactionGetReceipt(builder.setHeader(header));
    }

    @Override
    TransactionReceipt mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return TransactionReceipt.fromProtobuf(response.getTransactionGetReceipt().getReceipt());
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getTransactionGetReceipt().getHeader();
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getTransactionGetReceipt().getHeader();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetTransactionReceiptsMethod();
    }

    @Override
    boolean shouldRetry(Status status, Response response) {
        if (super.shouldRetry(status, response)) return true;

        var receiptStatus =
                Status.valueOf(response.getTransactionGetReceipt().getReceipt().getStatus());

        switch (receiptStatus) {
            case BUSY:
                // node is busy
            case UNKNOWN:
                // still in the node's queue
            case OK:
                // accepted but has not reached consensus
            case RECEIPT_NOT_FOUND:
                // has reached consensus but not generated
                return true;

            default:
                return false;
        }
    }
}
