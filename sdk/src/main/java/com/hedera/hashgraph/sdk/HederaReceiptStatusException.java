package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptQuery;
import com.hedera.hashgraph.sdk.proto.TransactionGetReceiptResponse;

/**
 * An Exception thrown on error status by {@link TransactionId#getReceipt(Client)}.
 *
 * The receipt is included, though only the {@link TransactionReceipt#status} field will be
 * initialized; all the getters should throw.
 */
public final class HederaReceiptStatusException extends Exception {

    public final ResponseCodeEnum responseCode;
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
        this.responseCode = responseCode;
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
                TransactionId.fromProtobuf(receiptQuery.getTransactionID()),
                TransactionReceipt.fromProtobuf(receiptResponse.getReceipt()));
        }
    }

    @Override
    public String getMessage() {
        return "receipt for transaction " + transactionId + " contained error status " + Status.valueOf(responseCode);
    }
}
