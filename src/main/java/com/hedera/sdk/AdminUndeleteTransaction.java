package com.hedera.sdk;

import com.hedera.sdk.proto.AdminUndeleteTransactionBody;
import com.hedera.sdk.proto.FileServiceGrpc;
import com.hedera.sdk.proto.Transaction;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

public final class AdminUndeleteTransaction extends TransactionBuilder<AdminUndeleteTransaction> {

    private final AdminUndeleteTransactionBody.Builder builder;

    public AdminUndeleteTransaction(Client client) {
        super(client);
        builder = bodyBuilder.getAdminUndeleteBuilder();
    }

    public AdminUndeleteTransaction setID(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public AdminUndeleteTransaction setID(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getAdminUndeleteMethod();
    }

    @Override
    protected void doValidate() {
        requireExactlyOne(
            ".setID() required",
            ".setID() may take a contract ID OR a file ID",
            builder.getContractIDOrBuilder(),
            builder.getFileIDOrBuilder()
        );
    }
}
