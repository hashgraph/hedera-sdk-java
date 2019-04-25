package com.hedera.hashgraph.sdk.account;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetClaimQuery`
public final class AccountClaimQuery extends QueryBuilder<CryptoGetClaimResponse> {
    private final CryptoGetClaimQuery.Builder builder;

    public AccountClaimQuery(Client client) {
        super(client);
        builder = inner.getCryptoGetClaimBuilder();
    }

    AccountClaimQuery() {
        super(null);
        builder = inner.getCryptoGetClaimBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountClaimQuery setAccount(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    public AccountClaimQuery setHash(byte[] hash) {
        builder.setHash(ByteString.copyFrom(hash));
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetClaimMethod();
    }

    @Override
    protected CryptoGetClaimResponse fromResponse(Response raw) {
        return raw.getCryptoGetClaim();
    }
}
