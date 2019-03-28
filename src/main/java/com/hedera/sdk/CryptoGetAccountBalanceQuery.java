package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class CryptoGetAccountBalanceQuery
        extends QueryBuilder<CryptoGetAccountBalanceResponse> {
    private final com.hedera.sdk.proto.CryptoGetAccountBalanceQuery.Builder builder;

    public CryptoGetAccountBalanceQuery() {
        super(Response::getCryptogetAccountBalance);
        builder = inner.getCryptogetAccountBalanceBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public CryptoGetAccountBalanceQuery setAccount(AccountId account) {
        builder.setAccountID(account.inner);

        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
