package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class CryptoGetInfoQuery extends QueryBuilder<CryptoGetInfoResponse> {
    private final com.hedera.sdk.proto.CryptoGetInfoQuery.Builder builder;

    public CryptoGetInfoQuery() {
        super(Response::getCryptoGetInfo);
        builder = inner.getCryptoGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public CryptoGetInfoQuery setAccount(AccountId account) {
        builder.setAccountID(account.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }
}
