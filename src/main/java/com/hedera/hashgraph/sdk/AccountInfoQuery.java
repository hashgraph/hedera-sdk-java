package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class AccountInfoQuery extends QueryBuilder<AccountInfo, AccountInfoQuery> {
    private final CryptoGetInfoQuery.Builder builder;

    public AccountInfoQuery() {
        builder = CryptoGetInfoQuery.newBuilder();
    }

    public AccountInfoQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProtobuf());
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setCryptoGetInfo(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetInfo().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getCryptoGetInfo().getHeader();
    }

    @Override
    protected AccountInfo mapResponse(Response response) {
        return AccountInfo.fromProtobuf(response.getCryptoGetInfo().getAccountInfo());
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }
}
