package com.hedera.sdk;

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import java.time.Duration;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>> {
    protected com.hedera.sdk.proto.Transaction.Builder inner =
            com.hedera.sdk.proto.Transaction.newBuilder();

    private static final int MAX_MEMO_LENGTH = 100;

    protected TransactionBuilder() {
        // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
        setTransactionFee(100_000);
        setTransactionValidDuration(Duration.ofMinutes(2));
    }

    /**
     * Sets the ID for this transaction, which includes the payer's account (the account paying the
     * transaction fee). If two transactions have the same transactionID, they won't both have an
     * effect.
     */
    public T setTransactionId(TransactionId transactionId) {
        inner.getBodyBuilder().setTransactionID(transactionId.inner);
        return self();
    }

    /** Sets the account of the node that submits the transaction to the network. */
    public final T setNodeAccountId(AccountId accountId) {
        inner.getBodyBuilder().setNodeAccountID(accountId.inner);
        return self();
    }

    /**
     * Sets the fee that the client pays to execute this transaction, which is split between the
     * network and the node.
     */
    public final T setTransactionFee(long fee) {
        inner.getBodyBuilder().setTransactionFee(fee);
        return self();
    }

    /**
     * Sets the the duration that this transaction is valid for. The transaction must consensus
     * before this this elapses.
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        inner.getBodyBuilder()
                .setTransactionValidDuration(
                        com.hedera.sdk.proto.Duration.newBuilder()
                                .setSeconds(validDuration.getSeconds())
                                .setNanos(validDuration.getNano()));

        return self();
    }

    /**
     * Sets whether the transaction should generate a record. A receipt is always generated but a
     * record is optional.
     */
    public final T setGenerateRecord(boolean generateRecord) {
        inner.getBodyBuilder().setGenerateRecord(generateRecord);
        return self();
    }

    /**
     * Sets any notes or description that should be put into the transaction record (if one is
     * requested). Note that a max of length of 100 is enforced.
     */
    public final T setMemo(String memo) {
        if (memo.length() > MAX_MEMO_LENGTH) {
            throw new IllegalArgumentException("memo must not be longer than 100 characters");
        }

        inner.getBodyBuilder().setMemo(memo);
        return self();
    }

    public final com.hedera.sdk.proto.Transaction build() {
        return inner.build();
    }

    protected abstract io.grpc.MethodDescriptor<
                    com.hedera.sdk.proto.Transaction, com.hedera.sdk.proto.TransactionResponse>
            getMethod();

    public final Transaction sign(Ed25519PrivateKey privateKey) {
        return new Transaction(inner, getMethod()).sign(privateKey);
    }

    // Work around for java not recognized that this is completely safe
    // as T is required to extend this
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
