package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoGetLiveHashQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

/**
 * Requests a livehash associated to an account.
 */
public final class LiveHashQuery extends Query<LiveHash, LiveHashQuery> {
    private final CryptoGetLiveHashQuery.Builder builder;

    AccountId accountId;

    public LiveHashQuery() {
        builder = CryptoGetLiveHashQuery.newBuilder();
    }

    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * The account to which the livehash is associated
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public LiveHashQuery setAccountId(AccountId accountId) {
        this.accountId = accountId;
        return this;
    }

    public ByteString getHash() {
      return builder.getHash();
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
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.accountId, accountId);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }

        queryBuilder.setCryptoGetLiveHash(builder.setHeader(header));
    }

    @Override
    LiveHash mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return LiveHash.fromProtobuf(response.getCryptoGetLiveHash().getLiveHash());
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetLiveHash().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getCryptoGetLiveHash().getHeader();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
