package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.mirror.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicQuery;
import com.hedera.hashgraph.sdk.proto.mirror.ConsensusTopicResponse;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.StreamObserver;
import java8.util.function.Consumer;
import org.threeten.bp.Instant;

public class MirrorTopicQuery {
    private final ConsensusTopicQuery.Builder builder;

    public MirrorTopicQuery() {
        builder = ConsensusTopicQuery.newBuilder();
    }

    public MirrorTopicQuery setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());
        return this;
    }

    public MirrorTopicQuery setStartTime(Instant startTime) {
        builder.setConsensusStartTime(InstantConverter.toProtobuf(startTime));
        return this;
    }

    public MirrorTopicQuery setEndTime(Instant endTime) {
        builder.setConsensusEndTime(InstantConverter.toProtobuf(endTime));
        return this;
    }

    public MirrorTopicQuery setLimit(long limit) {
        builder.setLimit(limit);
        return this;
    }

    // TODO: Refactor into a base class when we add more mirror query types
    public MirrorSubscriptionHandle subscribe(
        MirrorClient mirrorClient,
        Consumer<MirrorTopicResponse> onNext,
        Consumer<Throwable> onError
    ) {
        ClientCall<ConsensusTopicQuery, ConsensusTopicResponse> call =
            mirrorClient.channel.newCall(ConsensusServiceGrpc.getSubscribeTopicMethod(), CallOptions.DEFAULT);

        MirrorSubscriptionHandle subscriptionHandle = new MirrorSubscriptionHandle(() -> {
            call.cancel("unsubscribed", null);
        });

        ClientCalls.asyncServerStreamingCall(call, builder.build(), new StreamObserver<ConsensusTopicResponse>() {
            @Override
            public void onNext(ConsensusTopicResponse consensusTopicResponse) {
                onNext.accept(new MirrorTopicResponse(consensusTopicResponse));
            }

            @Override
            public void onError(Throwable throwable) {
                onError.accept(throwable);
            }

            @Override
            public void onCompleted() {
                // Do nothing
            }
        });

        return subscriptionHandle;
    }
}
