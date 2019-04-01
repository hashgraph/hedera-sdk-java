package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetStakersQuery`
public class AccountStakersQuery extends QueryBuilder<CryptoGetStakersResponse> {
    private final com.hedera.sdk.proto.CryptoGetStakersQuery.Builder builder;

    public AccountStakersQuery() {
        super(Response::getCryptoGetProxyStakers);
        builder = inner.getCryptoGetProxyStakersBuilder();
    }

    public AccountStakersQuery setAccount(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected void doValidate() {
        require(builder.getAccountIDOrBuilder(), ".setAccount() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }
}
