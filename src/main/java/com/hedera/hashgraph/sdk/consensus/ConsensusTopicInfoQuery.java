package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.service.proto.java.ConsensusServiceGrpc;
import io.grpc.MethodDescriptor;

public final class ConsensusTopicInfoQuery extends QueryBuilder<ConsensusTopicInfo, ConsensusTopicInfoQuery> {
    private final com.hederahashgraph.api.proto.java.ConsensusGetTopicInfoQuery.Builder builder;

    public ConsensusTopicInfoQuery() {
        builder = inner.getConsensusGetTopicInfoBuilder();
    }

    public ConsensusTopicInfoQuery setTopicId(ConsensusTopicId id) {
        builder.setTopicID(id.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected void doValidate() {
        require(builder.hasTopicID(), ".setTopicId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return ConsensusServiceGrpc.getGetTopicInfoMethod();
    }

    @Override
    protected ConsensusTopicInfo fromResponse(Response response) {
        return ConsensusTopicInfo.fromResponse(response);
    }
}
