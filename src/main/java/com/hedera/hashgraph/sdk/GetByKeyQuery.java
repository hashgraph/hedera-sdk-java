package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

public final class GetByKeyQuery extends QueryBuilder<GetByKeyResponse, GetByKeyQuery> {
    private final com.hedera.hashgraph.sdk.proto.GetByKeyQuery.Builder builder = inner.getGetByKeyBuilder();

    GetByKeyQuery(Client client) {
        super(client);
    }

    GetByKeyQuery() {
        super((Client) null);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        // FIXME there is no service method that corresponds to this query
        throw new Error("not implemented");
    }

    @Override
    protected GetByKeyResponse fromResponse(Response raw) {
        return raw.getGetByKey();
    }

    @Override
    protected void doValidate() {
        require(builder.hasKey(), ".setKey() required");
    }

    public GetByKeyQuery setKey(Key publicKey) {
        builder.setKey(publicKey.toKeyProto());
        return this;
    }
}
