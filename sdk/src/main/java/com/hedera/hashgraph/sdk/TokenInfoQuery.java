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

import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TokenGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Initializes the TokenInfoQuery object.
 */
public class TokenInfoQuery extends com.hedera.hashgraph.sdk.Query<TokenInfo, TokenInfoQuery> {
    @Nullable
    TokenId tokenId = null;

    /**
     * Constructor.
     */
    public TokenInfoQuery() {
    }

    /**
     * Extract the token id.
     *
     * @return                          the token id
     */
    @Nullable
    public TokenId getTokenId() {
        return tokenId;
    }

    /**
     * Sets the Token ID for which information is requested.
     *
     * @param tokenId                           The TokenId to be set
     * @return {@code this}
     */
    public TokenInfoQuery setTokenId(TokenId tokenId) {
        Objects.requireNonNull(tokenId);
        this.tokenId = tokenId;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = TokenGetInfoQuery.newBuilder();
        if (tokenId != null) {
            builder.setToken(tokenId.toProtobuf());
        }

        queryBuilder.setTokenGetInfo(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getTokenGetInfo().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getTokenGetInfo().getHeader();
    }

    @Override
    TokenInfo mapResponse(Response response, AccountId nodeId, Query request) {
        return TokenInfo.fromProtobuf(response.getTokenGetInfo());
    }

    @Override
    MethodDescriptor<Query, Response> getMethodDescriptor() {
        return TokenServiceGrpc.getGetTokenInfoMethod();
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `Token_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }
}
