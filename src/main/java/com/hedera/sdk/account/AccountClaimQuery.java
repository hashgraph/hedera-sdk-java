package com.hedera.sdk.account;

import com.google.protobuf.ByteString;
import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetClaimQuery`
public final class AccountClaimQuery extends QueryBuilder<CryptoGetClaimResponse> {
    private final CryptoGetClaimQuery.Builder builder;

    public AccountClaimQuery(Client client) {
        super(client, Response::getCryptoGetClaim);
        builder = inner.getCryptoGetClaimBuilder();
    }

    AccountClaimQuery() {
        super(null, Response::getCryptoGetClaim);
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
        require(builder.hasAccountID(), ".setAccount() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetClaimMethod();
    }
}
