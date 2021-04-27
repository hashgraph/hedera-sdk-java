package com.hedera.hashgraph.sdk.schedule;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import io.grpc.MethodDescriptor;

public class ScheduleSignTransaction extends SingleTransactionBuilder<ScheduleSignTransaction> {
    private final ScheduleSignTransactionBody.Builder builder = bodyBuilder.getScheduleSignBuilder();

    public ScheduleSignTransaction() {
        super();
    }

    public ScheduleSignTransaction setScheduleId(ScheduleId scheduleId) {
        builder.setScheduleID(scheduleId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ScheduleServiceGrpc.getSignScheduleMethod();
    }

    @Override
    protected void doValidate() {
        // Do nothing
    }
}
