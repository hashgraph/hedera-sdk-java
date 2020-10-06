package com.hedera.hashgraph.sdk.token;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.HederaThrowable;
import com.hedera.hashgraph.sdk.QueryBuilder;

import java.util.function.Consumer;

import io.grpc.MethodDescriptor;

/**
 * Gets information about Token instance
 */
public final class TokenInfoQuery extends QueryBuilder<TokenInfo, TokenInfoQuery> {
    private final com.hedera.hashgraph.proto.TokenGetInfoQuery.Builder builder;

    public TokenInfoQuery() {
        super();
        builder = inner.getTokenGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    /**
     * The token for which information is requested. If invalid token is provided,
     * INVALID_TOKEN_ID response is returned.
     *
     * @param token
     * @return TokenInfoQuery
     */
    public TokenInfoQuery setTokenId(TokenId token) {
        builder.setToken(token.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return TokenServiceGrpc.getGetTokenInfoMethod();
    }

    @Override
    protected TokenInfo extractResponse(Response raw) {
        return TokenInfo.fromResponse(raw);
    }

    @Override
    public long getCost(Client client) throws HederaStatusException, HederaNetworkException {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `TOKEN_DELETED` back instead.
        return Math.max(super.getCost(client), 25);
    }

    @Override
    public void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        // see above
        super.getCostAsync(client, (cost) -> withCost.accept(Math.min(cost, 25)), onError);
    }
}
