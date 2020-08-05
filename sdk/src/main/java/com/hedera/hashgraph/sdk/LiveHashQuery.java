package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoGetLiveHashQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

/**
 * Requests a livehash associated to an account.
 */
public final class LiveHashQuery extends QueryBuilder<LiveHash, LiveHashQuery> {
    private final CryptoGetLiveHashQuery.Builder builder;

    public LiveHashQuery() {
        builder = CryptoGetLiveHashQuery.newBuilder();
    }

    /**
     * The account to which the livehash is associated
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public LiveHashQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProtobuf());
        return this;
    }

    /**
     * The SHA-384 data in the livehash
     *
     * @param hash The array of bytes to be set as hash
     * @return {@code this}
     */
    public LiveHashQuery setHash(byte[] hash) {
        builder.setHash(ByteString.copyFrom(hash));
        return this;
    }

    @Override
    void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setCryptoGetLiveHash(builder.setHeader(header));
    }

    @Override
    LiveHash mapResponse(Response response, AccountId nodeId, Query request) {
        return LiveHash.fromProtobuf(response.getCryptoGetLiveHash().getLiveHash());
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetLiveHash().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(Query request) {
        return request.getCryptoGetLiveHash().getHeader();
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
