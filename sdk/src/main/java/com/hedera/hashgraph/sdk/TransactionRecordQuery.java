package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetRecordQuery;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.Objects;

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
    @Nullable
    TransactionId transactionId = null;

    public TransactionRecordQuery() {
    }

    @Nullable
    @Override
    public TransactionId getTransactionIdInternal() {
        return transactionId;
    }

    /**
     * Set the ID of the transaction for which the record is requested.
     *
     * @param transactionId The TransactionId to be set
     * @return {@code this}
     */
    public TransactionRecordQuery setTransactionId(TransactionId transactionId) {
        Objects.requireNonNull(transactionId);
        this.transactionId = transactionId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (transactionId != null) {
            Objects.requireNonNull(transactionId.accountId).validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = TransactionGetRecordQuery.newBuilder();
        if (transactionId != null) {
            builder.setTransactionID(transactionId.toProtobuf());
        }

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
    ExecutionState shouldRetry(Status status, Response response) {
        var retry = super.shouldRetry(status, response);
        if (retry != ExecutionState.Finished) {
            return retry;
        }

        switch (status) {
            case BUSY:
            case UNKNOWN:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;
            case OK:
                // When fetching payment an `OK` in there query header means the cost is in the response
                if (paymentTransactions == null || paymentTransactions.isEmpty()) {
                    return ExecutionState.Finished;
                } else {
                    break;
                }
            default:
                return ExecutionState.Error;
        }

        var receiptStatus =
            Status.valueOf(response.getTransactionGetRecord().getTransactionRecord().getReceipt().getStatus());

        switch (receiptStatus) {
            case BUSY:
            case UNKNOWN:
            case OK:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;

            default:
                return ExecutionState.Finished;
        }
    }
}
