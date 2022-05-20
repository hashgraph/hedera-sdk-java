package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptQuery;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
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

    @Nullable
    private TransactionId transactionId = null;
    private boolean includeChildren = false;
    private boolean includeDuplicates = false;

    /**
     * Constructor.
     */
    public TransactionReceiptQuery() {
    }

    /**
     * @return                          the transaction id
     */
    @Override
    @Nullable
    public TransactionId getTransactionIdInternal() {
        return transactionId;
    }

    /**
     * Set the ID of the transaction for which the receipt is being requested.
     *
     * @param transactionId The TransactionId to be set
     * @return {@code this}
     */
    public TransactionReceiptQuery setTransactionId(TransactionId transactionId) {
        Objects.requireNonNull(transactionId);
        this.transactionId = transactionId;
        return this;
    }

    /**
     * @return                          should children be included
     */
    public boolean getIncludeChildren() {
        return includeChildren;
    }

    /**
     * Whether the response should include the records of any child transactions spawned by the
     * top-level transaction with the given transactionID.
     *
     * @param value The value that includeChildren should be set to; true to include children, false to exclude
     * @return {@code this}
     */
    public TransactionReceiptQuery setIncludeChildren(boolean value) {
        includeChildren = value;
        return this;
    }

    /**
     * @return                          should duplicates be included
     */
    public boolean getIncludeDuplicates() {
        return includeDuplicates;
    }

    /**
     * Whether records of processing duplicate transactions should be returned along with the record
     * of processing the first consensus transaction with the given id whose status was neither
     * INVALID_NODE_ACCOUNT nor INVALID_PAYER_SIGNATURE or, if no such
     * record exists, the record of processing the first transaction to reach consensus with the
     * given transaction id.
     *
     * @param value The value that includeDuplicates should be set to; true to include duplicates, false to exclude
     * @return {@code this}
     */
    public TransactionReceiptQuery setIncludeDuplicates(boolean value) {
        includeDuplicates = value;
        return this;
    }

    @Override
    boolean isPaymentRequired() {
        return false;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (transactionId != null) {
            Objects.requireNonNull(transactionId.accountId).validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = TransactionGetReceiptQuery.newBuilder()
            .setIncludeChildReceipts(includeChildren)
            .setIncludeDuplicates(includeDuplicates);
        if (transactionId != null) {
            builder.setTransactionID(transactionId.toProtobuf());
        }

        queryBuilder.setTransactionGetReceipt(builder.setHeader(header));
    }

    @Override
    Status mapResponseStatus(Response response) {
        var preCheckCode = response.getTransactionGetReceipt().getHeader().getNodeTransactionPrecheckCode();

        return Status.valueOf(preCheckCode);
    }

    @Override
    TransactionReceipt mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        var receiptResponse = response.getTransactionGetReceipt();
        var duplicates = mapReceiptList(receiptResponse.getDuplicateTransactionReceiptsList());
        var children = mapReceiptList(receiptResponse.getChildTransactionReceiptsList());
        return TransactionReceipt.fromProtobuf(response.getTransactionGetReceipt().getReceipt(), duplicates, children);
    }

    /**
     * Create a list of transaction receipts from a protobuf.
     *
     * @param protoReceiptList          the protobuf
     * @return                          the list of transaction receipts
     */
    private static List<TransactionReceipt> mapReceiptList(
        List<com.hedera.hashgraph.sdk.proto.TransactionReceipt> protoReceiptList
    ) {
        List<TransactionReceipt> outList = new ArrayList<>(protoReceiptList.size());
        for (var protoReceipt : protoReceiptList) {
            outList.add(TransactionReceipt.fromProtobuf(protoReceipt));
        }
        return outList;
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
        switch (status) {
            case BUSY:
            case UNKNOWN:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;

            case OK:
                break;

            default:
                return ExecutionState.RequestError;
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

            default:
                return ExecutionState.Success;
        }
    }
}
