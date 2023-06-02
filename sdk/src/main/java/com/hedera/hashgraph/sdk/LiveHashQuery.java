/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoGetLiveHashQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

/**
 * Requests a livehash associated to an account.
 */
public final class LiveHashQuery extends Query<LiveHash, LiveHashQuery> {
    @Nullable
    private AccountId accountId;
    private byte[] hash = {};

    /**
     * Constructor.
     */
    public LiveHashQuery() {
    }

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
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = CryptoGetLiveHashQuery.newBuilder();
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }
        builder.setHash(ByteString.copyFrom(hash));

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
