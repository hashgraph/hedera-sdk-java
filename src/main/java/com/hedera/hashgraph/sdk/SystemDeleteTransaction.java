package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.contract.ContractId;
import com.hedera.hashgraph.sdk.file.FileId;
import com.hedera.hashgraph.proto.SystemDeleteTransactionBody;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.FileServiceGrpc;

import java.time.Instant;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

public final class SystemDeleteTransaction extends TransactionBuilder<SystemDeleteTransaction> {
    private final SystemDeleteTransactionBody.Builder builder = bodyBuilder.getSystemDeleteBuilder();

    public SystemDeleteTransaction() { super(); }

    public SystemDeleteTransaction setFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    public SystemDeleteTransaction setContractId(ContractId contractId) {
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
