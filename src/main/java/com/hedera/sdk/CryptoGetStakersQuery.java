package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class CryptoGetStakersQuery extends QueryBuilder<CryptoGetStakersResponse> {
    private final com.hedera.sdk.proto.CryptoGetStakersQuery.Builder builder;

    public CryptoGetStakersQuery() {
        super(Response::getCryptoGetProxyStakers);
        builder = inner.getCryptoGetProxyStakersBuilder();
    }

    public CryptoGetStakersQuery setAccount(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }
}
