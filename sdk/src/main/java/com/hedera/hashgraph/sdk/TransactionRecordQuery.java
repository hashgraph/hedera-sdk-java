package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetRecordQuery;
import io.grpc.MethodDescriptor;

/**
 * Get the record for a transaction.
 * <p>
 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it.
 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for
 * what it created. If the transaction called a smart contract function, then the record contains the result of
 * that call. If the transaction was a cryptocurrency transfer, then the record includes the TransferList
 * which gives the details of that transfer. If the transaction didn't return anything that should be
 * in the record, then the results field will be set to nothing.
 */
public final class TransactionRecordQuery extends Query<TransactionRecord, TransactionRecordQuery> {
    private final TransactionGetRecordQuery.Builder builder;

    public TransactionRecordQuery() {
        this.builder = TransactionGetRecordQuery.newBuilder();
    }

    public TransactionId getTransactionId() {
      return TransactionId.fromProtobuf(builder.getTransactionID());
    }

    /**
     * Set the ID of the transaction for which the record is requested.
     *
     * @return {@code this}
     * @param transactionId The TransactionId to be set
     */
    public TransactionRecordQuery setTransactionId(TransactionId transactionId) {
        builder.setTransactionID(transactionId.toProtobuf());
        return this;
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setTransactionGetRecord(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getTransactionGetRecord().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getTransactionGetRecord().getHeader();
    }

    @Override
    TransactionRecord mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return TransactionRecord.fromProtobuf(response.getTransactionGetRecord().getTransactionRecord());
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetTxRecordByTxIDMethod();
    }

    @Override
    boolean shouldRetry(Status status, Response response) {
        if (super.shouldRetry(status, response)) return true;

        var receiptStatus =
            Status.valueOf(response.getTransactionGetRecord().getTransactionRecord().getReceipt().getStatus());

        switch (receiptStatus) {
            case BUSY:
                // node is busy
            case UNKNOWN:
                // still in the node's queue
            case OK:
                // accepted but has not reached consensus
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                // has reached consensus but not generated
                return true;

            default:
                return false;
        }
    }
}
