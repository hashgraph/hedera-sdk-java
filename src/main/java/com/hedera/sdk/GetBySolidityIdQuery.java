package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class GetBySolidityIdQuery extends QueryBuilder<GetBySolidityIDResponse> {
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
    protected GetBySolidityIDResponse mapResponse(Response raw) throws HederaException {
        return raw.getGetBySolidityID();
    }

    @Override
    protected void doValidate() {
        require(builder.getSolidityID(), ".setSolidityId() required");
    }
}
