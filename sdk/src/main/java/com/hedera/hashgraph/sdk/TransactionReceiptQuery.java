package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptQuery;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.Objects;

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

    public TransactionId getTransactionId() {
      return TransactionId.fromProtobuf(builder.getTransactionID());
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
    Status mapResponseStatus(Response response) {
        var preCheckCode = response.getTransactionGetReceipt().getHeader().getNodeTransactionPrecheckCode();

        return Status.valueOf(preCheckCode);
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
    ExecutionState shouldRetry(Status status, Response response) {
        var retry = super.shouldRetry(status, response);
        if (retry != ExecutionState.Finished) return retry;

        switch (status) {
            case BUSY:
            case UNKNOWN:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;

            case OK:
                break;

            default:
                return ExecutionState.Error;
        }

        var receiptStatus =
            Status.valueOf(response.getTransactionGetReceipt().getReceipt().getStatus());

        switch (receiptStatus) {
            case BUSY:
            case UNKNOWN:
            case OK:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;

            case SUCCESS:
                return ExecutionState.Finished;

            default:
                return ExecutionState.Error;
        }
    }

    @Override
    Exception mapStatusError(Status status, @Nullable TransactionId transactionId, Response response) {
        if (status != Status.OK) {
            return new PrecheckStatusException(status, transactionId);
        }

        // has reached consensus but not generated
        return new ReceiptStatusException(
            Objects.requireNonNull(transactionId),
            TransactionReceipt.fromProtobuf(response.getTransactionGetReceipt().getReceipt())
        );
    }
}
