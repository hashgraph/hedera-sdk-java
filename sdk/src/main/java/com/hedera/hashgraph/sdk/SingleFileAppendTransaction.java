package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileAppendTransactionBody;
import com.hedera.hashgraph.sdk.proto.FileID;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class SingleFileAppendTransaction extends Transaction<SingleFileAppendTransaction> {
    SingleFileAppendTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
    }

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
