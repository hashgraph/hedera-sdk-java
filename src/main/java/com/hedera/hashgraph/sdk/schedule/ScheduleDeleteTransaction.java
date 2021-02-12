package com.hedera.hashgraph.sdk.schedule;

import com.hedera.hashgraph.proto.ScheduleDeleteTransactionBody;
import com.hedera.hashgraph.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import io.grpc.MethodDescriptor;

public class ScheduleDeleteTransaction extends SingleTransactionBuilder<ScheduleDeleteTransaction> {
    private final ScheduleDeleteTransactionBody.Builder builder = bodyBuilder.getScheduleDeleteBuilder();

    public ScheduleDeleteTransaction() {
        super();
    }

    public ScheduleDeleteTransaction setScheduleId(ScheduleId scheduleId) {
        builder.setScheduleID(scheduleId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ScheduleServiceGrpc.getDeleteScheduleMethod();
    }

    @Override
    protected void doValidate() {
        // Do nothing
    }
}
