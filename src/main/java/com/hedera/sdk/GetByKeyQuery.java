package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
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

    public GetByKeyQuery setKey(Key publicKey) {
        builder.setKey(publicKey.toProtoKey());
        return this;
    }
}
