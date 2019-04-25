package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.proto.AdminDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.time.Instant;

public final class AdminDeleteTransaction extends TransactionBuilder<AdminDeleteTransaction> {
    private final AdminDeleteTransactionBody.Builder builder = bodyBuilder.getAdminDeleteBuilder();

    public AdminDeleteTransaction(Client client) {
        super(client);
    }

    AdminDeleteTransaction() {
        super(null);
    }

    public AdminDeleteTransaction setID(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public AdminDeleteTransaction setID(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    public AdminDeleteTransaction setExpirationTime(Instant timestamp) {
        builder.setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getAdminDeleteMethod();
    }

    @Override
    protected void doValidate() {
        requireExactlyOne(
            ".setID() required",
            ".setID() may take a contract ID OR a file ID",
            builder.hasContractID(),
            builder.hasFileID()
        );
    }
}
