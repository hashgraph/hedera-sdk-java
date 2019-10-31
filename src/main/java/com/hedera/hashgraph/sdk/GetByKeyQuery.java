package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.GetByKeyResponse;
import com.hederahashgraph.api.proto.java.Query;
import com.hederahashgraph.api.proto.java.QueryHeader;
import com.hederahashgraph.api.proto.java.Response;

import io.grpc.MethodDescriptor;

public final class GetByKeyQuery extends QueryBuilder<GetByKeyResponse, GetByKeyQuery> {
    private final com.hederahashgraph.api.proto.java.GetByKeyQuery.Builder builder = inner.getGetByKeyBuilder();

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

    public GetByKeyQuery setKey(PublicKey publicKey) {
        builder.setKey(publicKey.toKeyProto());
        return this;
    }
}
