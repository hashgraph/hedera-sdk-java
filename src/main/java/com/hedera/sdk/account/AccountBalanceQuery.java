package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetAccountBalanceQuery`
public final class AccountBalanceQuery extends QueryBuilder<CryptoGetAccountBalanceResponse> {
    private final com.hedera.sdk.proto.CryptoGetAccountBalanceQuery.Builder builder;

    public AccountBalanceQuery() {
        super(Response::getCryptogetAccountBalance);
        builder = inner.getCryptogetAccountBalanceBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountBalanceQuery setAccount(AccountId account) {
        builder.setAccountID(account.toProto());

        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
