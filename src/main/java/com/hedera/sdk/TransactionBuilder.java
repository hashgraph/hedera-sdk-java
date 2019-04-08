package com.hedera.sdk;

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.proto.TransactionBody;

import java.time.Duration;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>> extends ValidatedBuilder {
    protected com.hedera.sdk.proto.Transaction.Builder inner = com.hedera.sdk.proto.Transaction.newBuilder();
    protected final TransactionBody.Builder bodyBuilder = inner.getBodyBuilder();
    private final Client client;

    private static final int MAX_MEMO_LENGTH = 100;

    protected TransactionBuilder(Client client) {
        this.client = client;

        // todo: transaction fees should be defaulted to whatever the transaction fee schedule is
        setTransactionFee(client.defaultTxnFee);
        setTransactionValidDuration(client.defaultTxnValidDuration);
    }

    /**
     * Sets the ID for this transaction, which includes the payer's account (the account paying the
     * transaction fee). If two transactions have the same transactionID, they won't both have an
     * effect.
     */
    public T setTransactionId(TransactionId transactionId) {
        bodyBuilder
            .setTransactionID(transactionId.inner);
        return self();
    }

    /** Sets the account of the node that submits the transaction to the network. */
    public final T setNodeAccount(AccountId accountId) {
        bodyBuilder
            .setNodeAccountID(accountId.inner);
        return self();
    }

    /**
     * Sets the fee that the client pays to execute this transaction, which is split between the
     * network and the node.
     */
    public final T setTransactionFee(long fee) {
        bodyBuilder
            .setTransactionFee(fee);
        return self();
    }

    /**
     * Sets the the duration that this transaction is valid for. The transaction must consensus
     * before this this elapses.
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        bodyBuilder
            .setTransactionValidDuration(
                com.hedera.sdk.proto.Duration.newBuilder()
                    .setSeconds(validDuration.getSeconds())
                    .setNanos(validDuration.getNano())
            );

        return self();
    }

    /**
     * Sets whether the transaction should generate a record. A receipt is always generated but a
     * record is optional.
     */
    public final T setGenerateRecord(boolean generateRecord) {
        bodyBuilder
            .setGenerateRecord(generateRecord);
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

        bodyBuilder
            .setMemo(memo);
        return self();
    }

    protected abstract void doValidate();

    @Override
    public final void validate() {
        var bodyBuilder = this.bodyBuilder;
        require(bodyBuilder.hasTransactionID(), ".setTransactionId() required");
        require(bodyBuilder.hasNodeAccountID(), ".setNodeAccount() required");
        doValidate();
        checkValidationErrors("transaction builder failed validation");
    }

    final com.hedera.sdk.proto.Transaction build() {
        return inner.build();
    }

    protected abstract io.grpc.MethodDescriptor<com.hedera.sdk.proto.Transaction, com.hedera.sdk.proto.TransactionResponse> getMethod();

    public final Transaction sign(Ed25519PrivateKey privateKey) {
        if (!bodyBuilder.hasNodeAccountID()) {

        }

        validate();
        return new Transaction(client, inner, getMethod()).sign(privateKey);
    }

    // Work around for java not recognized that this is completely safe
    // as T is required to extend this
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
