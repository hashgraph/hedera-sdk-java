package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.QueryBuilder;

import io.grpc.MethodDescriptor;

public final class ConsensusTopicInfoQuery extends QueryBuilder<ConsensusTopicInfo, ConsensusTopicInfoQuery> {
    private final com.hedera.hashgraph.proto.ConsensusGetTopicInfoQuery.Builder builder;

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
    protected ConsensusTopicInfo extractResponse(Response raw) {
        return ConsensusTopicInfo.fromResponse(raw);
    }
}

