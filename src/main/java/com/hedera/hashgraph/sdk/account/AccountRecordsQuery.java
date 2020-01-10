package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetAccountRecordsQuery;
import com.hedera.hashgraph.proto.CryptoGetAccountRecordsResponse;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.QueryBuilder;

import io.grpc.MethodDescriptor;

/**
 * Get a list of {@link com.hedera.hashgraph.sdk.TransactionRecord}s involved with an account.
 *
 * @deprecated the result type of {@link CryptoGetAccountRecordsResponse} returned from the various
 * {@code execute[Async](...)} methods is changing in 1.0 to {@code List<TransactionRecord>}, which
 * is a breaking change. This class is not being removed.
 */
@Deprecated
// `CryptoGetAccountRecordsQuery`
public class AccountRecordsQuery extends QueryBuilder<CryptoGetAccountRecordsResponse, AccountRecordsQuery> {
    private final CryptoGetAccountRecordsQuery.Builder builder = inner.getCryptoGetAccountRecordsBuilder();

    public AccountRecordsQuery() {
        super(null);
    }

    public AccountRecordsQuery setAccountId(AccountId accountId) {
        builder.setAccountID(accountId.toProto());
        return this;
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    @Override
    protected void doValidate() {
        require(builder.hasAccountID(), ".setAccountId() required");
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return CryptoServiceGrpc.getGetAccountRecordsMethod();
    }

    @Override
    protected CryptoGetAccountRecordsResponse extractResponse(Response raw) {
        return raw.getCryptoGetAccountRecords();
    }
}
