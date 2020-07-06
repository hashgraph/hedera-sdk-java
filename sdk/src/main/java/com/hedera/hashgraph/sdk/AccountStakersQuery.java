package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetStakersQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;

/**
 * Get all the accounts that are proxy staking to this account.
 * For each of them, give the amount currently staked.
 *
 * This is not yet implemented, but will be in a future version of the API.
 */
public final class AccountStakersQuery extends QueryBuilder<List<ProxyStaker>, AccountStakersQuery> {
    private final CryptoGetStakersQuery.Builder builder;

    public AccountStakersQuery() {
        builder = CryptoGetStakersQuery.newBuilder();
    }

    /**
     * Sets the Account ID for which the records should be retrieved.
     *
     * @return {@code this}
     * @param accountId The AccountId to be set
     */
    public AccountStakersQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProtobuf());
        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setCryptoGetProxyStakers(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetProxyStakers().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getCryptoGetProxyStakers().getHeader();
    }

    @Override
    List<ProxyStaker> mapResponse(Response response) {
        var rawStakers = response.getCryptoGetProxyStakers().getStakers();
        var stakers = new ArrayList<ProxyStaker>(rawStakers.getProxyStakerCount());

        for (var i = 0; i < rawStakers.getProxyStakerCount(); ++i) {
            stakers.add(ProxyStaker.fromProtobuf(rawStakers.getProxyStaker(i)));
        }

        return stakers;
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }
}
