package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class GetBySolidityIdQuery extends QueryBuilder<GetBySolidityIDResponse> {
    private final GetBySolidityIDQuery.Builder builder;

    public GetBySolidityIdQuery() {
        super(Response::getGetBySolidityID);
        builder = inner.getGetBySolidityIDBuilder();
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
    MethodDescriptor<Query, Response> getMethod() {
        return SmartContractServiceGrpc.getGetBySolidityIDMethod();
    }
}
