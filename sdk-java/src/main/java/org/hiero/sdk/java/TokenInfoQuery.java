// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java;

import io.grpc.MethodDescriptor;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import org.hiero.sdk.java.proto.Query;
import org.hiero.sdk.java.proto.QueryHeader;
import org.hiero.sdk.java.proto.Response;
import org.hiero.sdk.java.proto.ResponseHeader;
import org.hiero.sdk.java.proto.TokenGetInfoQuery;
import org.hiero.sdk.java.proto.TokenServiceGrpc;

/**
 * Initializes the TokenInfoQuery object.
 */
public class TokenInfoQuery extends org.hiero.sdk.java.Query<TokenInfo, TokenInfoQuery> {
    @Nullable
    TokenId tokenId = null;

    /**
     * Constructor.
     */
    public TokenInfoQuery() {}

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
    void onMakeRequest(org.hiero.sdk.java.proto.Query.Builder queryBuilder, QueryHeader header) {
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
    QueryHeader mapRequestHeader(org.hiero.sdk.java.proto.Query request) {
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
