package com.hedera.sdk;

import com.hedera.sdk.proto.QueryHeader;

public class GetByKeyQuery extends QueryBuilder {
    private final com.hedera.sdk.proto.GetByKeyQuery.Builder builder;

    GetByKeyQuery() {
        builder = inner.getGetByKeyBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }
}
