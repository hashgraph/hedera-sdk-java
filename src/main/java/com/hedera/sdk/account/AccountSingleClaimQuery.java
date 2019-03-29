package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetClaimQuery`
public final class AccountSingleClaimQuery extends QueryBuilder<CryptoGetClaimResponse> {
    private final com.hedera.sdk.proto.CryptoGetClaimQuery.Builder builder;

    public AccountSingleClaimQuery() {
        super(Response::getCryptoGetClaim);
        builder = inner.getCryptoGetClaimBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountSingleClaimQuery setAccount(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetClaimMethod();
    }
}
