package com.hedera.sdk;

<<<<<<< HEAD
import com.hedera.sdk.crypto.IPublicKey;
=======
>>>>>>> 34b6ee5e064c006dfba5bc99d6e76ffe36ccaa6c
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

    public GetByKeyQuery setKey(IPublicKey publicKey) {
        builder.setKey(publicKey.toProtoKey());
        return this;
    }

    public GetByKeyQuery setKey(ContractId contractId) {
        builder.setKey(contractId.toProtoKey());
        return this;
    }
}
