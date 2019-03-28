package com.hedera.sdk;

import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class GetByKeyQuery extends QueryBuilder<GetByKeyResponse> {
    private final com.hedera.sdk.proto.GetByKeyQuery.Builder builder;

    GetByKeyQuery() {
        super(Response::getGetByKey);
        builder = inner.getGetByKeyBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        // FIXME there is no service method that corresponds to this query
        throw new Error("not implemented");
    }

    public GetByKeyQuery setKey(Key publicKey) {
        builder.setKey(publicKey.toKeyProto());
        return this;
    }
}
