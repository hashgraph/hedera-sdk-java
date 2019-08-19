package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hederahashgraph.api.proto.java.CryptoGetStakersQuery;
import com.hederahashgraph.api.proto.java.CryptoGetStakersResponse;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import io.grpc.MethodDescriptor;

// `CryptoGetStakersQuery`
public class AccountStakersQuery extends QueryBuilder<CryptoGetStakersResponse, AccountStakersQuery> {
    private final CryptoGetStakersQuery.Builder builder = inner.getCryptoGetProxyStakersBuilder();

    public AccountStakersQuery(Client client) {
        super(client);
    }

    AccountStakersQuery() {
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
