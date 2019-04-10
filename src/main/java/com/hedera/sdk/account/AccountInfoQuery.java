package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetInfoQuery`
public final class AccountInfoQuery extends QueryBuilder<AccountInfo> {
    private final com.hedera.sdk.proto.CryptoGetInfoQuery.Builder builder;

    public AccountInfoQuery(Client client) {
        super(client, AccountInfo::new);
        builder = inner.getCryptoGetInfoBuilder();
    }

    AccountInfoQuery() {
        super(null, AccountInfo::new);
        builder = inner.getCryptoGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountInfoQuery setAccount(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccount() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountInfoMethod();
    }
}
