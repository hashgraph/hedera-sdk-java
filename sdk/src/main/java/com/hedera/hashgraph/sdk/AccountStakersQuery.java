package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetStakersQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Get all the accounts that are proxy staking to this account.
 * For each of them, give the amount currently staked.
 *
 * This is not yet implemented, but will be in a future version of the API.
 */
public final class AccountStakersQuery extends Query<List<ProxyStaker>, AccountStakersQuery> {
    private final CryptoGetStakersQuery.Builder builder;

    @Nullable
    AccountId accountId = null;

    public AccountStakersQuery() {
        builder = CryptoGetStakersQuery.newBuilder();
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the Account ID for which the records should be retrieved.
     *
     * @return {@code this}
     * @param accountId The AccountId to be set
     */
    public AccountStakersQuery setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        this.accountId = accountId;
        return this;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (accountId != null) {
            accountId.validate(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }

        queryBuilder.setCryptoGetProxyStakers(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetProxyStakers().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getCryptoGetProxyStakers().getHeader();
    }

    @Override
    List<ProxyStaker> mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request, @Nullable NetworkName networkName) {
        var rawStakers = response.getCryptoGetProxyStakers().getStakers();
        var stakers = new ArrayList<ProxyStaker>(rawStakers.getProxyStakerCount());

        for (var i = 0; i < rawStakers.getProxyStakerCount(); ++i) {
            stakers.add(ProxyStaker.fromProtobuf(rawStakers.getProxyStaker(i), networkName));
        }

        return stakers;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }
}
