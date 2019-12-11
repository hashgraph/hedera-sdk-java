package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.CryptoGetStakersQuery;
import com.hedera.hashgraph.proto.CryptoGetStakersResponse;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;

import io.grpc.MethodDescriptor;

// `CryptoGetStakersQuery`
public class AccountStakersQuery extends QueryBuilder<CryptoGetStakersResponse, AccountStakersQuery> {
    private final CryptoGetStakersQuery.Builder builder = inner.getCryptoGetProxyStakersBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public AccountStakersQuery(Client client) {
        super(client);
    }

    public AccountStakersQuery() {
        super(null);
    }

    public AccountStakersQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }

    @Override
    protected CryptoGetStakersResponse fromResponse(Response raw) {
        return raw.getCryptoGetProxyStakers();
    }
}
