package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ConsensusGetTopicInfoQuery;
import com.hedera.hashgraph.sdk.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class TopicInfoQuery extends QueryBuilder<TopicInfo, TopicInfoQuery> {
    private final ConsensusGetTopicInfoQuery.Builder builder;

    public TopicInfoQuery() {
        builder = ConsensusGetTopicInfoQuery.newBuilder();
    }

    public TopicInfoQuery setTopicId(TopicId topicId) {
        builder.setTopicID(topicId.toProtobuf());

        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setConsensusGetTopicInfo(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getConsensusGetTopicInfo().getHeader();
    }

    @Override
    protected TopicInfo mapResponse(Response response) {
        return TopicInfo.fromProtobuf(response.getConsensusGetTopicInfo());
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return ConsensusServiceGrpc.getGetTopicInfoMethod();
    }
}
