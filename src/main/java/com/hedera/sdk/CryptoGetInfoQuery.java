package com.hedera.sdk;

import com.hedera.sdk.proto.QueryHeader;

public final class CryptoGetInfoQuery extends QueryBuilder {
    private final com.hedera.sdk.proto.CryptoGetInfoQuery.Builder builder;

    public CryptoGetInfoQuery() {
        builder = inner.getCryptoGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public CryptoGetInfoQuery setAccount(AccountId account) {
        builder.setAccountID(account.inner);
        return this;
    }
}
