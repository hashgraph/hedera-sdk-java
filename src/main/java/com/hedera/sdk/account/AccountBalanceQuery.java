package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetAccountBalanceQuery`
public final class AccountBalanceQuery extends QueryBuilder<CryptoGetAccountBalanceResponse> {
    private final CryptoGetAccountBalanceQuery.Builder builder = inner.getCryptogetAccountBalanceBuilder();

    AccountBalanceQuery() {
        super(null, Response::getCryptogetAccountBalance);
    }

    public AccountBalanceQuery(Client client) {
        super(client, Response::getCryptogetAccountBalance);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountBalanceQuery setAccount(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccount() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }
}
