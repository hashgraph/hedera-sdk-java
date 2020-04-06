package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ContractCallLocalQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import io.grpc.MethodDescriptor;

// TODO: ContractFunctionResult
public final class ContractCallQuery extends QueryBuilder<Void, ContractCallQuery> {
    private final ContractCallLocalQuery.Builder builder;

    public ContractCallQuery() {
        builder = ContractCallLocalQuery.newBuilder();
    }

    public ContractCallQuery setContractId(ContractId contractId) {
        builder.setContractID(contractId.toProtobuf());
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setContractCallLocal(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getContractCallLocal().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getContractCallLocal().getHeader();
    }

    @Override
    protected Void mapResponse(Response response) {
        return null;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return SmartContractServiceGrpc.getContractCallLocalMethodMethod();
    }
}
