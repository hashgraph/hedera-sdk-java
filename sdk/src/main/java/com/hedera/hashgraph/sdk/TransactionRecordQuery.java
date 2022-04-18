/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import com.hedera.hashgraph.sdk.proto.TransactionGetRecordQuery;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Get the record for a transaction.
 * <p>
 * If the transaction requested a record, then the record lasts for one hour, and a state proof is available for it.
 * If the transaction created an account, file, or smart contract instance, then the record will contain the ID for
 * what it created. If the transaction called a smart contract function, then the record contains the result of
 * that call. If the transaction was a cryptocurrency transfer, then the record includes the TransferList
 * which gives the details of that transfer. If the transaction didn't return anything that should be
 * in the record, then the results field will be set to nothing.
 */
public final class TransactionRecordQuery extends Query<TransactionRecord, TransactionRecordQuery> {
    @Nullable
    private TransactionId transactionId = null;
    private boolean includeChildren = false;
    private boolean includeDuplicates = false;

    public TransactionRecordQuery() {
    }

    @Nullable
    @Override
    public TransactionId getTransactionIdInternal() {
        return transactionId;
    }

    /**
     * Set the ID of the transaction for which the record is requested.
     *
     * @param transactionId The TransactionId to be set
     * @return {@code this}
     */
    public TransactionRecordQuery setTransactionId(TransactionId transactionId) {
        Objects.requireNonNull(transactionId);
        this.transactionId = transactionId;
        return this;
    }

    public boolean getIncludeDuplicates() {
        return includeDuplicates;
    }

    /**
     * Whether records of processing duplicate transactions should be returned along with the record
     * of processing the first consensus transaction with the given id whose status was neither
     * INVALID_NODE_ACCOUNT nor INVALID_PAYER_SIGNATURE or, if no such
     * record exists, the record of processing the first transaction to reach consensus with the
     * given transaction id.
     *
     * @param value The value that includeDuplicates should be set to; true to include duplicates, false to exclude
     * @return {@code this}
     */
    public TransactionRecordQuery setIncludeDuplicates(boolean value) {
        includeDuplicates = value;
        return this;
    }

    public boolean getIncludeChildren() {
        return includeChildren;
    }

    /**
     * Whether the response should include the records of any child transactions spawned by the
     * top-level transaction with the given transactionID.
     *
     * @param value The value that includeChildren should be set to; true to include children, false to exclude
     * @return {@code this}
     */
    public TransactionRecordQuery setIncludeChildren(boolean value) {
        includeChildren = value;
        return this;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (transactionId != null) {
            Objects.requireNonNull(transactionId.accountId).validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = TransactionGetRecordQuery.newBuilder()
            .setIncludeChildRecords(includeChildren)
            .setIncludeDuplicates(includeDuplicates);
        if (transactionId != null) {
            builder.setTransactionID(transactionId.toProtobuf());
        }

        queryBuilder.setTransactionGetRecord(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getTransactionGetRecord().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getTransactionGetRecord().getHeader();
    }

    @Override
    TransactionRecord mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        var recordResponse = response.getTransactionGetRecord();
        List<TransactionRecord> children = mapRecordList(recordResponse.getChildTransactionRecordsList());
        List<TransactionRecord> duplicates = mapRecordList(recordResponse.getDuplicateTransactionRecordsList());
        return TransactionRecord.fromProtobuf(recordResponse.getTransactionRecord(), children, duplicates);
    }

    private List<TransactionRecord> mapRecordList(
        List<com.hedera.hashgraph.sdk.proto.TransactionRecord> protoRecordList
    ) {
        List<TransactionRecord> outList = new ArrayList<>(protoRecordList.size());
        for (var protoRecord : protoRecordList) {
            outList.add(TransactionRecord.fromProtobuf(protoRecord));
        }
        return outList;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return CryptoServiceGrpc.getGetTxRecordByTxIDMethod();
    }

    @Override
    ExecutionState shouldRetry(Status status, Response response) {
        var retry = super.shouldRetry(status, response);
        if (retry != ExecutionState.Success) {
            return retry;
        }

        switch (status) {
            case BUSY:
            case UNKNOWN:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;
            case OK:
                // When fetching payment an `OK` in there query header means the cost is in the response
                if (paymentTransactions == null || paymentTransactions.isEmpty()) {
                    return ExecutionState.Success;
                } else {
                    break;
                }
            default:
                return ExecutionState.RequestError;
        }

        var receiptStatus =
            Status.valueOf(response.getTransactionGetRecord().getTransactionRecord().getReceipt().getStatus());

        switch (receiptStatus) {
            case BUSY:
            case UNKNOWN:
            case OK:
            case RECEIPT_NOT_FOUND:
            case RECORD_NOT_FOUND:
                return ExecutionState.Retry;

            default:
                return ExecutionState.Success;
        }
    }
}
