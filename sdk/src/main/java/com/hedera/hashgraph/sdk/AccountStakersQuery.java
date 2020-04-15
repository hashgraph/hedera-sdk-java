package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetStakersQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class AccountStakersQuery extends QueryBuilder<ProxyStaker[], AccountStakersQuery> {
    private final CryptoGetStakersQuery.Builder builder;

    public AccountStakersQuery() {
        builder = CryptoGetStakersQuery.newBuilder();
    }

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
    ProxyStaker[] mapResponse(Response response) {
        var rawStakers = response.getCryptoGetProxyStakers().getStakers();
        var stakers = new ProxyStaker[rawStakers.getProxyStakerCount()];

        for (var i = 0; i < stakers.length; ++i) {
            stakers[i] = ProxyStaker.fromProtobuf(rawStakers.getProxyStaker(i));
        }

        return stakers;
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetStakersByAccountIDMethod();
    }
}
