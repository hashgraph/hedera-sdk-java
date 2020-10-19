package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.QueryBuilder;

import com.hedera.hashgraph.sdk.token.TokenId;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.Map;

public final class TokenBalanceQuery extends QueryBuilder<Map<TokenId, Long>, TokenBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder = inner.getCryptogetAccountBalanceBuilder();

    public TokenBalanceQuery() {
        super();

        // a payment transaction is required but is not processed so it can have zero value
        setQueryPayment(0);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public TokenBalanceQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }

    @Override
    protected Map<TokenId, Long> extractResponse(Response raw) {
        Map<TokenId, Long> map = new HashMap<>(raw.getCryptogetAccountBalance().getTokenBalancesCount());

        for (TokenBalance balance : raw.getCryptogetAccountBalance().getTokenBalancesList()) {
            map.put(new TokenId(balance.getTokenId()), balance.getBalance());
        }

        return map;
    }
}
