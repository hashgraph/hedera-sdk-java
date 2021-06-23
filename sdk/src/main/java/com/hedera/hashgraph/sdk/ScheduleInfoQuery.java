package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ScheduleGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

public class ScheduleInfoQuery extends com.hedera.hashgraph.sdk.Query<ScheduleInfo, ScheduleInfoQuery> {
    private final ScheduleGetInfoQuery.Builder builder;

    ScheduleId scheduleId;

    public ScheduleInfoQuery() {
        builder = ScheduleGetInfoQuery.newBuilder();
    }

    public ScheduleInfoQuery setScheduleId(ScheduleId scheduleId) {
        this.scheduleId = scheduleId;
        return this;
    }

    public ScheduleId getScheduleId() {
        return scheduleId;
    }

    @Override
    void validateNetworkOnIds(@Nullable NetworkName networkName) {
        EntityIdHelper.validateNetworkOnIds(this.scheduleId, networkName);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (scheduleId != null) {
            builder.setScheduleID(scheduleId.toProtobuf());
        }

        queryBuilder.setScheduleGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getScheduleGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getScheduleGetInfo().getHeader();
    }

    @Override
    ScheduleInfo mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return ScheduleInfo.fromProtobuf(response.getScheduleGetInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return ScheduleServiceGrpc.getGetScheduleInfoMethod();
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `Token_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }
}
