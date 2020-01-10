package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetAccountRecordsQuery;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.TransactionRecord;

import java.util.List;
import java.util.stream.Collectors;

import io.grpc.MethodDescriptor;

/**
 * Get a list of {@link com.hedera.hashgraph.sdk.TransactionRecord}s involved with an account.
 */
// `CryptoGetAccountRecordsQuery`
public final class AccountRecordsQuery extends QueryBuilder<List<TransactionRecord>, AccountRecordsQuery> {
    private final CryptoGetAccountRecordsQuery.Builder builder = inner.getCryptoGetAccountRecordsBuilder();

    public AccountRecordsQuery() {
        super();
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
    protected List<TransactionRecord> extractResponse(Response raw) {
        return raw.getCryptoGetAccountRecords()
            .getRecordsList()
            .stream()
            .map(TransactionRecord::new)
            .collect(Collectors.toList());
    }
}
