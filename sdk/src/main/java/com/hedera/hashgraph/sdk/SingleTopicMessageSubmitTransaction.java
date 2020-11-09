package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.List;

class SingleTopicMessageSubmitTransaction extends Transaction<SingleTopicMessageSubmitTransaction> {
    SingleTopicMessageSubmitTransaction(HashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction> txs) {
        super(txs);
    }

    SingleTopicMessageSubmitTransaction(
        List<AccountId> nodeIds,
        TransactionBody.Builder bodyBuilder,
        TopicID topicId,
        ConsensusMessageChunkInfo chunkInfo,
        ByteString message
    ) {
        super(bodyBuilder.setConsensusSubmitMessage(ConsensusSubmitMessageTransactionBody.newBuilder()
            .setTopicID(topicId)
            .setMessage(message)
            .setChunkInfo(chunkInfo)
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
        return ConsensusServiceGrpc.getSubmitMessageMethod();
    }
}
