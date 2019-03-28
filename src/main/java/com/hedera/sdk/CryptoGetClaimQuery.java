package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class CryptoGetClaimQuery extends QueryBuilder<CryptoGetClaimResponse> {
    private final com.hedera.sdk.proto.CryptoGetClaimQuery.Builder builder;

    public CryptoGetClaimQuery() {
        super(Response::getCryptoGetClaim);
        builder = inner.getCryptoGetClaimBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public CryptoGetClaimQuery setAccount(AccountId account) {
        builder.setAccountID(account.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetClaimMethod();
    }
}
