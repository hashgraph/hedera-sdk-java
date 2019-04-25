package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetInfoQuery`
public final class AccountInfoQuery extends QueryBuilder<AccountInfo> {
    private final com.hedera.hashgraph.sdk.proto.CryptoGetInfoQuery.Builder builder;

    public AccountInfoQuery(Client client) {
        super(client);
        builder = inner.getCryptoGetInfoBuilder();
    }

    AccountInfoQuery() {
        super(null);
        builder = inner.getCryptoGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountInfoQuery setAccountId(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }

    @Override
    protected AccountInfo fromResponse(Response raw) {
        return new AccountInfo(raw);
    }
}
