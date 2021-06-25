package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoGetAccountRecordsQuery;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Get all the records for an account for any transfers into it and out of it,
 * that were above the threshold, during the last 25 hours.
 */
public final class AccountRecordsQuery extends Query<List<TransactionRecord>, AccountRecordsQuery> {
    private final CryptoGetAccountRecordsQuery.Builder builder;

    AccountId accountId;

    public AccountRecordsQuery() {
        this.builder = CryptoGetAccountRecordsQuery.newBuilder();
    }

    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * Sets the account ID for which the records should be retrieved.
     *
     * @return {@code this}
     * @param accountId The AccountId to be set
     */
    public AccountRecordsQuery setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        this.accountId = accountId;
        return this;
    }

    @Override
    void validateNetworkOnIds(Client client) {
        if (accountId != null) {
            accountId.validate(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (accountId != null) {
            builder.setAccountID(accountId.toProtobuf());
        }

        queryBuilder.setCryptoGetAccountRecords(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getCryptoGetAccountRecords().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getCryptoGetAccountRecords().getHeader();
    }

    @Override
    List<TransactionRecord> mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request, @Nullable NetworkName networkName) {
        var rawTransactionRecords = response.getCryptoGetAccountRecords().getRecordsList();
        var transactionRecords = new ArrayList<TransactionRecord>(rawTransactionRecords.size());

        for (var record : rawTransactionRecords) {
            transactionRecords.add(TransactionRecord.fromProtobuf(record, networkName));
        }

        return transactionRecords;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetAccountRecordsMethod();
    }
}
