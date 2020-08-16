package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.ConsensusMessageChunkInfo;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ConsensusSubmitMessageTransactionBody;
import com.hedera.hashgraph.sdk.proto.TopicID;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.Arrays;
import java.util.List;

class SingleTopicMessageSubmitTransaction extends Transaction<SingleTopicMessageSubmitTransaction> {
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
