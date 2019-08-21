package com.hedera.hashgraph.sdk;

import com.hederahashgraph.api.proto.java.GetBySolidityIDQuery;
import com.hederahashgraph.api.proto.java.GetBySolidityIDResponse;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

public final class GetBySolidityIdQuery extends QueryBuilder<GetBySolidityIDResponse, GetBySolidityIdQuery> {
    private final GetBySolidityIDQuery.Builder builder = inner.getGetBySolidityIDBuilder();

    public GetBySolidityIdQuery(Client client) {
        super(client);
    }

    GetBySolidityIdQuery() {
        super((Client) null);
    }

    public GetBySolidityIdQuery setSolidityId(String solidityId) {
        builder.setSolidityID(solidityId);
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetBySolidityIDMethod();
    }

    @Override
    protected GetBySolidityIDResponse fromResponse(Response raw) {
        return raw.getGetBySolidityID();
    }

    @Override
    protected void doValidate() {
        require(builder.getSolidityID(), ".setSolidityId() required");
    }
}
