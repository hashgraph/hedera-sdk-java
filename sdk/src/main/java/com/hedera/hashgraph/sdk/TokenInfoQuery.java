package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

import java.util.concurrent.CompletableFuture;

public class TokenInfoQuery extends com.hedera.hashgraph.sdk.Query<TokenInfo, TokenInfoQuery> {
    private final TokenGetInfoQuery.Builder builder;

    public TokenInfoQuery() {
        builder = TokenGetInfoQuery.newBuilder();
    }

    /**
     * Sets the Token ID for which information is requested.
     *
     * @return {@code this}
     * @param tokenId The TokenId to be set
     */
    public TokenInfoQuery setTokenId(TokenId tokenId) {
        builder.setToken(tokenId.toProtobuf());

        return this;
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
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
    TokenInfo mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
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
