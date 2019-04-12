package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `CryptoGetAccountRecordsQuery`
public class AccountRecordsQuery extends QueryBuilder<CryptoGetAccountRecordsResponse> {
    private final CryptoGetAccountRecordsQuery.Builder builder = inner.getCryptoGetAccountRecordsBuilder();

    public AccountRecordsQuery(Client client) {
        super(client);
    }

    AccountRecordsQuery() {
        super(null);
    }

    public AccountRecordsQuery setAccount(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccount() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountRecordsMethod();
    }

    @Override
    protected CryptoGetAccountRecordsResponse mapResponse(Response raw) throws HederaException {
        return raw.getCryptoGetAccountRecords();
    }
}
