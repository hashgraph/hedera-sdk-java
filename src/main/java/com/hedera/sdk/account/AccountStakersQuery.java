package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetStakersQuery`
public class AccountStakersQuery extends QueryBuilder<CryptoGetStakersResponse> {
    private final CryptoGetStakersQuery.Builder builder = inner.getCryptoGetProxyStakersBuilder();

    public AccountStakersQuery(Client client) {
        super(client);
    }

    AccountStakersQuery() {
        super(null);
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
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }

    @Override
    protected CryptoGetStakersResponse mapResponse(Response raw) throws HederaException {
        return raw.getCryptoGetProxyStakers();
    }
}
