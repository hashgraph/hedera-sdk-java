package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.account.AccountId;

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

    HederaPrecheckStatusException(AccountId nodeId, ResponseCodeEnum responseCode, TransactionId transactionId) {
        super(nodeId, responseCode);
        this.transactionId = transactionId;
    }

    static void throwIfExceptional(AccountId nodeId, ResponseCodeEnum responseCode, TransactionId transactionId) throws HederaPrecheckStatusException {
        if (isCodeExceptional(responseCode)) {
            throw new HederaPrecheckStatusException(nodeId, responseCode, transactionId);
        }
    }

    @Override
    public String getMessage() {
        return "transaction " + transactionId + " failed precheck with status " + status;
    }
}
