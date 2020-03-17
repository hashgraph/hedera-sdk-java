package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.proto.TransactionGetReceiptQuery;
import com.hedera.hashgraph.proto.TransactionGetReceiptResponse;

/**
 * A {@link HederaStatusException}, thrown on error status by {@link TransactionId#getReceipt(Client)}.
 *
 * The receipt is included, though only the {@link TransactionReceipt#status} field will be
 * initialized; all the getters should throw.
 */
public class HederaReceiptStatusException extends HederaStatusException {
    /**
     * The ID of the transaction that failed, in case that context is no longer available
     * (e.g. the exception was bubbled up).
     */
    public final TransactionId transactionId;

    /**
     * The receipt of the transaction that failed; the only initialized field is
     * {@link TransactionReceipt#status}.
     */
    public final TransactionReceipt receipt;

    HederaReceiptStatusException(ResponseCodeEnum responseCode, TransactionId transactionId, TransactionReceipt receipt) {
        super(responseCode);
        this.transactionId = transactionId;
        this.receipt = receipt;
    }

    static boolean isCodeExceptional(ResponseCodeEnum responseCode) {
        return responseCode != ResponseCodeEnum.SUCCESS;
    }

    static void throwIfExceptional(TransactionGetReceiptQuery receiptQuery, TransactionGetReceiptResponse receiptResponse) throws HederaReceiptStatusException {
        ResponseCodeEnum status = receiptResponse.getReceipt().getStatus();

        if (isCodeExceptional(receiptResponse.getReceipt().getStatus())) {
            throw new HederaReceiptStatusException(
                status,
                new TransactionId(receiptQuery.getTransactionIDOrBuilder()),
                new TransactionReceipt(receiptResponse.getReceipt()));
        }
    }

    @Override
    public String getMessage() {
        return "receipt for transaction " + transactionId + " contained error status " + status;
    }
}
