package com.hedera.sdk.file;

import com.hedera.sdk.ContractId;
import com.hedera.sdk.FileId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

// `AdminUndeleteTransaction`
public final class FileAdminUndeleteTransaction
        extends TransactionBuilder<FileAdminUndeleteTransaction> {
    public FileAdminUndeleteTransaction setID(FileId fileId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setFileID(fileId.toProto());
        return this;
    }

    public FileAdminUndeleteTransaction setID(ContractId contractId) {
        inner.getBodyBuilder().getAdminUndeleteBuilder().setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getAdminUndeleteMethod();
    }
}
