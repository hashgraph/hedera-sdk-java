package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;

/**
 * A {@link HederaStatusException}, thrown specifically by a transaction precheck (validation
 * on the node it was submitted to).
 */
public class HederaPrecheckStatusException extends HederaStatusException {
    /**
     * The ID of the transaction that failed, in case that context is no longer available
     * (e.g. the exception was bubbled up).
     */
    public final TransactionId transactionId;

    HederaPrecheckStatusException(ResponseCodeEnum responseCode, TransactionId transactionId) {
        super(responseCode);
        this.transactionId = transactionId;
    }

    static void throwIfExceptional(ResponseCodeEnum responseCode, TransactionId transactionId) throws HederaPrecheckStatusException {
        if (isCodeExceptional(responseCode)) {
            throw new HederaPrecheckStatusException(responseCode, transactionId);
        }
    }

    @Override
    public String getMessage() {
        return "transaction " + transactionId + " failed precheck with status " + status;
    }
}
