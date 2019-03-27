package com.hedera.sdk;

import com.hedera.sdk.proto.QueryHeader;

public final class CryptoGetAccountBalanceQuery extends QueryBuilder {
    private final com.hedera.sdk.proto.CryptoGetAccountBalanceQuery.Builder builder;

    public CryptoGetAccountBalanceQuery() {
        builder = inner.getCryptogetAccountBalanceBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public CryptoGetAccountBalanceQuery setAccount(AccountId account) {
        builder.setAccountID(account.inner);

        return this;
    }
}
