package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.CryptoGetAccountBalanceQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

// `CryptoGetAccountBalanceQuery`
public final class AccountBalanceQuery extends QueryBuilder<Long, AccountBalanceQuery> {
    private final CryptoGetAccountBalanceQuery.Builder builder = inner.getCryptogetAccountBalanceBuilder();

    public AccountBalanceQuery(@Nullable Client client) {
        super(client);
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public AccountBalanceQuery setAccountId(AccountId account) {
        builder.setAccountID(account.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getCryptoGetBalanceMethod();
    }

    @Override
    protected Long fromResponse(Response raw) {
        return raw.getCryptogetAccountBalance()
            .getBalance();
    }

    @Override
    protected boolean isPaymentRequired() {
        return true;
    }
}
