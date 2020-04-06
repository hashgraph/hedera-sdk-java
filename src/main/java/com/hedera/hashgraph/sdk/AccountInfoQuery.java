package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class AccountInfoQuery extends QueryBuilder<AccountInfo, AccountInfoQuery> {
    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return null;
    }

    @Override
    protected AccountInfo mapResponse(Response response) {
        return null;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return null;
    }
}
