package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

public final class GetTopicInfoQuery extends QueryBuilder<TopicInfo, GetTopicInfoQuery> {
    private final ConsensusGetTopicInfoQuery.Builder builder = inner.getConsensusGetTopicInfoBuilder();

    public GetTopicInfoQuery(@Nullable Client client) {
        super(client);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public GetTopicInfoQuery setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), ".setTopicID() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return ConsensusServiceGrpc.getGetTopicInfoMethod();
    }

    @Override
    protected TopicInfo fromResponse(Response raw) {
        return new TopicInfo(raw);
    }
}
