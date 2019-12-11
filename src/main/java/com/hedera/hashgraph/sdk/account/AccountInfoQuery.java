package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;

import io.grpc.MethodDescriptor;

// `CryptoGetInfoQuery`
public final class AccountInfoQuery extends QueryBuilder<AccountInfo, AccountInfoQuery> {
    private final com.hedera.hashgraph.proto.CryptoGetInfoQuery.Builder builder;

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public AccountInfoQuery(Client client) {
        super(client);
        builder = inner.getCryptoGetInfoBuilder();
    }

    public AccountInfoQuery() {
        super();
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
        return AccountInfo.fromResponse(raw);
    }
}
