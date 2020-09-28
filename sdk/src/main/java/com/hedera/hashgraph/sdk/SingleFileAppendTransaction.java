package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.List;

public class SingleFileAppendTransaction extends Transaction<SingleFileAppendTransaction> {
    SingleFileAppendTransaction(
        List<AccountId> nodeIds,
        TransactionBody.Builder bodyBuilder,
        FileID fileId,
        ByteString content
    ) {
        super(bodyBuilder.setFileAppend(FileAppendTransactionBody.newBuilder()
            .setFileID(fileId)
            .setContents(content)
            .build()));

        this.nodeIds = nodeIds;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        // do nothing, the transaction was created directly in the constructor
        return true;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getAppendContentMethod();
    }
}
