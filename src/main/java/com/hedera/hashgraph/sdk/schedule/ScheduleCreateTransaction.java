package com.hedera.hashgraph.sdk.schedule;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import io.grpc.MethodDescriptor;

public class ScheduleCreateTransaction extends SingleTransactionBuilder<ScheduleCreateTransaction> {
    private final ScheduleCreateTransactionBody.Builder builder = bodyBuilder.getScheduleCreateBuilder();

    public ScheduleCreateTransaction() {
        super();
    }

    public ScheduleCreateTransaction setScheduledTransaction(com.hedera.hashgraph.sdk.Transaction transaction) throws InvalidProtocolBufferException {
        ScheduleCreateTransaction other = transaction.schedule();
        this.builder.setScheduledTransactionBody(other.builder.getScheduledTransactionBody());
        return this;
    }

    public ScheduleCreateTransaction setScheduledTransactionBody(SchedulableTransactionBody body) throws InvalidProtocolBufferException {
        this.builder.setScheduledTransactionBody(body);
        return this;
    }

    public ScheduleCreateTransaction setAdminKey(PublicKey publicKey) {
        builder.setAdminKey(publicKey.toKeyProto());
        return this;
    }

    public ScheduleCreateTransaction setMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    public ScheduleCreateTransaction setPayerAccountId(AccountId payerAccountId) {
        builder.setPayerAccountID(payerAccountId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ScheduleServiceGrpc.getCreateScheduleMethod();
    }

    @Override
    protected void doValidate() {
        // Do nothing
    }
}
