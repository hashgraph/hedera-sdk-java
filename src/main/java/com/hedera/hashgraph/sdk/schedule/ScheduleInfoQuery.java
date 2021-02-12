package com.hedera.hashgraph.sdk.schedule;

import com.hedera.hashgraph.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.HederaThrowable;
import com.hedera.hashgraph.sdk.QueryBuilder;

import java.util.function.Consumer;

import io.grpc.MethodDescriptor;

public final class ScheduleInfoQuery extends QueryBuilder<ScheduleInfo, ScheduleInfoQuery> {
    private final com.hedera.hashgraph.proto.ScheduleGetInfoQuery.Builder builder;

    public ScheduleInfoQuery() {
        super();
        builder = inner.getScheduleGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public ScheduleInfoQuery setScheduleId(ScheduleId schedule) {
        builder.setScheduleID(schedule.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasScheduleID(), ".setScheduleId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return ScheduleServiceGrpc.getGetScheduleInfoMethod();
    }

    @Override
    protected ScheduleInfo extractResponse(Response raw) {
        return ScheduleInfo.fromResponse(raw);
    }

    @Override
    public long getCost(Client client) throws HederaStatusException, HederaNetworkException {
        // deleted schedules return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `ACCOUNT_DELETED` back instead.
        return Math.max(super.getCost(client), 25);
    }

    @Override
    public void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        // see above
        super.getCostAsync(client, (cost) -> withCost.accept(Math.min(cost, 25)), onError);
    }
}
