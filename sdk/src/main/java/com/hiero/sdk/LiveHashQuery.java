// SPDX-License-Identifier: Apache-2.0
package com.hiero.sdk;

import com.google.protobuf.ByteString;
import com.hiero.sdk.proto.CryptoGetLiveHashQuery;
import com.hiero.sdk.proto.CryptoServiceGrpc;
import com.hiero.sdk.proto.QueryHeader;
import com.hiero.sdk.proto.Response;
import com.hiero.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Requests a livehash associated to an account.
 */
public final class LiveHashQuery extends Query<LiveHash, LiveHashQuery> {
    @Nullable
    private AccountId accountId = null;

    private byte[] hash = {};

    /**
     * Constructor.
     */
    public LiveHashQuery() {}

    /**
     * Extract the account id.
     *
     * @return                          the account id
     */
    @Nullable
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
        Objects.requireNonNull(accountId);
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the hash.
     *
     * @return                          the hash
     */
    public ByteString getHash() {
        return ByteString.copyFrom(hash);
    }

    /**
     * The SHA-384 data in the livehash
     *
     * @param hash The array of bytes to be set as hash
     * @return {@code this}
     */
    public LiveHashQuery setHash(byte[] hash) {
        this.hash = Arrays.copyOf(hash, hash.length);
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hiero.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = CryptoGetLiveHashQuery.newBuilder();
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }
        builder.setHash(ByteString.copyFrom(hash));

        queryBuilder.setCryptoGetLiveHash(builder.setHeader(header));
    }

    @Override
    LiveHash mapResponse(Response response, AccountId nodeId, com.hiero.sdk.proto.Query request) {
        return LiveHash.fromProtobuf(response.getCryptoGetLiveHash().getLiveHash());
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetLiveHash().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hiero.sdk.proto.Query request) {
        return request.getCryptoGetLiveHash().getHeader();
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
