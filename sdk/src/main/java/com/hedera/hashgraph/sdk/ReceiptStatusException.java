package com.hedera.hashgraph.sdk;

/**
 * An Exception thrown on error status by {@link TransactionId#getReceipt(Client)}.
 * <p>
 * The receipt is included, though only the {@link TransactionReceipt#status} field will be
 * initialized; all the getters should throw.
 */
public class ReceiptStatusException extends Exception {
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

    ReceiptStatusException(TransactionId transactionId, TransactionReceipt receipt) {
        this.transactionId = transactionId;
        this.receipt = receipt;
    }

    @Override
    public String getMessage() {
        return "receipt for transaction " + transactionId + " raised status " + receipt.status;
    }
}
