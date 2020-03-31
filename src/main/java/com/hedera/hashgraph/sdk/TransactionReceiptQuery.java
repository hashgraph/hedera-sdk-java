package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.TransactionGetReceiptQuery;

import java.time.Duration;

import io.grpc.MethodDescriptor;

public final class TransactionReceiptQuery extends QueryBuilder<TransactionReceipt, TransactionReceiptQuery> {
    private final TransactionGetReceiptQuery.Builder builder = inner.getTransactionGetReceiptBuilder();

    public TransactionReceiptQuery() {
        super();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return inner.getTransactionGetReceiptBuilder()
            .getHeaderBuilder();
    }

    public TransactionReceiptQuery setTransactionId(TransactionId transactionId) {
        builder.setTransactionID(transactionId.toProto());
        return this;
    }

    @Override
    protected boolean shouldRetry(HederaThrowable e) {
        if (super.shouldRetry(e)) {
            return true;
        } else if (!(e instanceof HederaStatusException)) {
            return false;
        }

        switch (((HederaStatusException) e).status) {
            // still in the node's queue
            case Unknown:
            // accepted but has not reached consensus
            case Ok:
            // has reached consensus but not generated
            case ReceiptNotFound:
            // BUSY is covered by super call
                return true;
            default:
                return false;
        }
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetTransactionReceiptsMethod();
    }

    @Override
    protected TransactionReceipt extractResponse(Response raw) {
        return new TransactionReceipt(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasTransactionID(), ".setTransactionId() required");
    }

    @Override
    protected Duration getDefaultTimeout() {
        // receipt lives for 3 minutes after consensus which we can't know ahead of time
        // validDuration plus the receipt time should be long enough
        return Transaction.MAX_VALID_DURATION.plus(Duration.ofMinutes(3));
    }

    @Override
    protected boolean isPaymentRequired() {
        return false;
    }
}
