package com.hedera.sdk;

import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Instant;

public final class AdminDeleteTransaction extends TransactionBuilder<AdminDeleteTransaction> {
    public AdminDeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setFileID(fileId.toProto());
        return this;
    }

    public AdminDeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setContractID(contractId.toProto());
        return this;
    }

    public AdminDeleteTransaction setExpirationTime(Instant timestamp) {
        inner.getBodyBuilder()
                .getAdminDeleteBuilder()
                .setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getAdminDeleteMethod();
    }
}
