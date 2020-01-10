package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetStakersQuery;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.QueryBuilder;

import java.util.List;
import java.util.stream.Collectors;

import io.grpc.MethodDescriptor;

/**
 * Get all the accounts that are proxy staking to a given account.
 */
// `CryptoGetStakersQuery`
public class AccountStakersQuery extends QueryBuilder<List<AccountProxyStaker>, AccountStakersQuery> {
    private final CryptoGetStakersQuery.Builder builder = inner.getCryptoGetProxyStakersBuilder();

    public AccountStakersQuery() {
        super();
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
    protected List<AccountProxyStaker> extractResponse(Response raw) {
        return raw.getCryptoGetProxyStakers()
            .getStakers()
            .getProxyStakerList()
            .stream()
            .map(AccountProxyStaker::new)
            .collect(Collectors.toList());
    }
}
