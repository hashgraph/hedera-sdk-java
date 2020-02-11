package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.proto.TransactionGetRecordResponse;

/**
 * A {@link HederaStatusException}, thrown on error status by {@link TransactionId#getRecord(Client)}.
 *
 * The record is included which could contain useful context for the error, such as errors
 * returned by {@link com.hedera.hashgraph.sdk.contract.ContractExecuteTransaction}.
 */
public class HederaRecordStatusException extends HederaStatusException {
    /**
     * The ID of the transaction that failed, in case that context is no longer available
     * (e.g. the exception was bubbled up).
     */
    public final TransactionId transactionId;

    /**
     * The record that was fetched; it may still contain useful context (such as contract execution
     * logs).
     */
    public final TransactionRecord record;

    HederaRecordStatusException(ResponseCodeEnum responseCode, TransactionRecord record) {
        super(responseCode);
        this.record = record;
        this.transactionId = record.transactionId;
    }

    static void throwIfExceptional(TransactionGetRecordResponse recordResponse) throws HederaRecordStatusException {
        ResponseCodeEnum status = recordResponse.getTransactionRecord().getReceipt().getStatus();

        if (isCodeExceptional(status)) {
            throw new HederaRecordStatusException(
                status,
                new TransactionRecord(recordResponse.getTransactionRecord()));
        }
    }

    @Override
    public String getMessage() {
        return "record for transaction " + transactionId + " contained error status " + status
            + "\nthis Exception instance contains the record that was fetched";
    }
}
