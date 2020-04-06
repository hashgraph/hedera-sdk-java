package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class ContractInfoQuery extends QueryBuilder<ContractInfo, ContractInfoQuery> {
    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return null;
    }

    @Override
    protected ContractInfo mapResponse(Response response) {
        return null;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return null;
    }
}
