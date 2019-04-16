package com.hedera.sdk.account;

import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetInfoQuery`
public final class AccountInfoQuery extends QueryBuilder<AccountInfo> {
    private final com.hedera.sdk.proto.CryptoGetInfoQuery.Builder builder;

    public AccountInfoQuery(Client client) {
        super(client);
        builder = inner.getCryptoGetInfoBuilder();
    }

    AccountInfoQuery() {
        super(null);
        builder = inner.getCryptoGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountInfoQuery setAccountId(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }

    @Override
    protected AccountInfo mapResponse(Response raw) throws HederaException {
        return new AccountInfo(raw);
    }
}
