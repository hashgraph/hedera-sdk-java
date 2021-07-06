package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusGetTopicInfoQuery;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Retrieve the latest state of a topic.
 * <p>
 * This method is unrestricted and allowed on any topic by any payer account.
 */
public final class TopicInfoQuery extends Query<TopicInfo, TopicInfoQuery> {
    private final ConsensusGetTopicInfoQuery.Builder builder;

    @Nullable
    TopicId topicId = null;

    public TopicInfoQuery() {
        builder = ConsensusGetTopicInfoQuery.newBuilder();
    }

    @Nullable
    public TopicId getTopicId() {
        return topicId;
    }

    /**
     * Set the topic to retrieve info about (the parameters and running state of).
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicInfoQuery setTopicId(TopicId topicId) {
        Objects.requireNonNull(topicId);
        this.topicId = topicId;
        return this;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (topicId != null) {
            topicId.validate(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (topicId != null) {
            builder.setTopicID(topicId.toProtobuf());
        }

        queryBuilder.setConsensusGetTopicInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    TopicInfo mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request, @Nullable NetworkName networkName) {
        return TopicInfo.fromProtobuf(response.getConsensusGetTopicInfo(), networkName);
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return ConsensusServiceGrpc.getGetTopicInfoMethod();
    }
}
