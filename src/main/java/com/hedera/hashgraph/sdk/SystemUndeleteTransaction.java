package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.api.proto.java.SystemUndeleteTransactionBody;

import io.grpc.MethodDescriptor;

public final class SystemUndeleteTransaction extends TransactionBuilder<SystemUndeleteTransaction> {

    private final SystemUndeleteTransactionBody.Builder builder = bodyBuilder.getSystemUndeleteBuilder();

    public SystemUndeleteTransaction(Client client) {
        super(client);
    }

    SystemUndeleteTransaction() {
        super(null);
    }

    public SystemUndeleteTransaction setID(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public SystemUndeleteTransaction setID(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getSystemUndeleteMethod();
    }

    @Override
    protected void doValidate() {
        requireExactlyOne(
            ".setID() required",
            ".setID() may take a contract ID OR a file ID",
            builder.hasContractID(),
            builder.hasFileID());
    }
}
