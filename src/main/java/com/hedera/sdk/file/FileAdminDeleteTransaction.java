package com.hedera.sdk.file;

import com.hedera.sdk.ContractId;
import com.hedera.sdk.FileId;
import com.hedera.sdk.TimestampHelper;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Instant;

// `AdminDeleteTransaction`
public final class FileAdminDeleteTransaction
        extends TransactionBuilder<FileAdminDeleteTransaction> {
    public FileAdminDeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setFileID(fileId.toProto());
        return this;
    }

    public FileAdminDeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminDeleteBuilder().setContractID(contractId.toProto());
        return this;
    }

    public FileAdminDeleteTransaction setExpirationTime(Instant timestamp) {
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
