package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.GetBySolidityIDQuery;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;

import io.grpc.MethodDescriptor;

/**
 * Look up the ID of an entity (account, contract, file) by its Solidity address.
 */
public final class GetBySolidityIdQuery extends QueryBuilder<HederaEntityInfo, GetBySolidityIdQuery> {
    private final GetBySolidityIDQuery.Builder builder = inner.getGetBySolidityIDBuilder();

    public GetBySolidityIdQuery() {
        super();
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
    protected HederaEntityInfo extractResponse(Response raw) {
        return new HederaEntityInfo(raw.getGetBySolidityID());
    }

    @Override
    protected void doValidate() {
        require(builder.getSolidityID(), ".setSolidityId() required");
    }
}
