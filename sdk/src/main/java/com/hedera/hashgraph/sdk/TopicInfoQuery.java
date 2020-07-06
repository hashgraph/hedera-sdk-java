package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusGetTopicInfoQuery;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

/**
 * Retrieve the latest state of a topic.
 * <p>
 * This method is unrestricted and allowed on any topic by any payer account.
 */
public final class TopicInfoQuery extends QueryBuilder<TopicInfo, TopicInfoQuery> {
    private final ConsensusGetTopicInfoQuery.Builder builder;

    public TopicInfoQuery() {
        builder = ConsensusGetTopicInfoQuery.newBuilder();
    }

    /**
     * Set the topic to retrieve info about (the parameters and running state of).
     *
     * @return {@code this}
     * @param topicId The TopicId to be set
     */
    public TopicInfoQuery setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());

        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setConsensusGetTopicInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    TopicInfo mapResponse(Response response) {
        return TopicInfo.fromProtobuf(response.getConsensusGetTopicInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return ConsensusServiceGrpc.getGetTopicInfoMethod();
    }
}
