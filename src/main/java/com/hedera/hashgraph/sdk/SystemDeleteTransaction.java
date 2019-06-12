package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Transaction;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.api.proto.java.SystemDeleteTransactionBody;

import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.time.Instant;

public final class SystemDeleteTransaction extends TransactionBuilder<SystemDeleteTransaction> {
    private final SystemDeleteTransactionBody.Builder builder = bodyBuilder.getSystemDeleteBuilder();

    public SystemDeleteTransaction(@Nullable Client client) {
        super(client);
    }

    public SystemDeleteTransaction setID(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public SystemDeleteTransaction setID(ContractId contractId) {
        builder.setContractID(contractId.toProto());
        return this;
    }

    public SystemDeleteTransaction setExpirationTime(Instant timestamp) {
        builder.setExpirationTime(TimestampHelper.timestampSecondsFrom(timestamp));
        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return FileServiceGrpc.getSystemDeleteMethod();
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
