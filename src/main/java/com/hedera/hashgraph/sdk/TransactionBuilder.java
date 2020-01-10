package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.proto.TransactionBody;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.account.AccountId;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>>
    extends HederaCall<com.hedera.hashgraph.proto.Transaction, TransactionResponse, TransactionId, T>
{
    protected final com.hedera.hashgraph.proto.Transaction.Builder inner = com.hedera.hashgraph.proto.Transaction.newBuilder();
    protected final TransactionBody.Builder bodyBuilder = TransactionBody.newBuilder();

    {
        setTransactionValidDuration(Transaction.MAX_VALID_DURATION);
    }

    private static final int MAX_MEMO_LENGTH = 100;

    private Duration validDuration = Transaction.MAX_VALID_DURATION;

    /**
     * Sets the ID for this transaction, which includes the payer's account (the account paying the
     * transaction fee). If two transactions have the same transactionID, they won't both have an
     * effect.
     */
    public T setTransactionId(TransactionId transactionId) {
        bodyBuilder.setTransactionID(transactionId.toProto());
        return self();
    }

    /**
     * Sets the account of the node that submits the transaction to the network.
     */
    public final T setNodeAccountId(AccountId accountId) {
        bodyBuilder.setNodeAccountID(accountId.toProto());
        return self();
    }

    /**
     * Sets the maximum fee that the client is willing to pay to execute this transaction, which is
     * split between the network and the node.
     * <p>
     * The actual fee assessed may be less than this, in which case you will only be charged
     * that amount. An error is thrown if the assessed fee is greater than this.
     * <p>
     * The calculation of the fee depends on the type of the transaction and its parameters,
     * as well as the current fee schedule of the network.
     * <p>
     * Defaults to the value of {@link Client#setMaxTransactionFee(long)}.
     */
    public final T setMaxTransactionFee(Hbar fee) {
        bodyBuilder.setTransactionFee(fee.asTinybar());
        return self();
    }

    /**
     * Sets the maximum fee, in tinybar, that the client is willing to pay to execute this
     * transaction, which is split between the network and the node.
     * <p>
     * The actual fee assessed may be less than this, in which case you will only be charged
     * that amount. An error is thrown if the assessed fee is greater than this.
     * <p>
     * The calculation of the fee depends on the type of the transaction and its parameters,
     * as well as the current fee schedule of the network.
     * <p>
     * Defaults to the value of {@link Client#setMaxTransactionFee(long)}.
     */
    public final T setMaxTransactionFee(long fee) {
        bodyBuilder.setTransactionFee(fee);
        return self();
    }

    /**
     * Sets the the duration that this transaction is valid for. The transaction must reach
     * consensus before this elapses.
     * <p>
     * The duration will be capped at 2 minutes as that is the maximum {@code validDuration} for
     * the network.
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        Duration actual = validDuration;

        if (Transaction.MAX_VALID_DURATION.compareTo(validDuration) < 0) {
            actual = Transaction.MAX_VALID_DURATION;
        }

        bodyBuilder.setTransactionValidDuration(DurationHelper.durationFrom(actual));
        this.validDuration = actual;

        return self();
    }

    /**
     * Sets any notes or description that should be put into the transaction record (if one is
     * requested). Note that a max of length of 100 is enforced.
     */
    public final T setTransactionMemo(String memo) {
        if (memo.length() > MAX_MEMO_LENGTH) {
            throw new IllegalArgumentException("memo must not be longer than 100 characters");
        }

        bodyBuilder.setMemo(memo);
        return self();
    }

    protected abstract void doValidate();

    @Override
    public final com.hedera.hashgraph.proto.Transaction toProto() {
        return build(null).toProto();
    }

    @Internal
    public final com.hedera.hashgraph.proto.Transaction toProto(boolean requireSignature) {
        return build(null).toProto(requireSignature);
    }

    @Override
    protected final void localValidate() {
        TransactionBody.Builder bodyBuilder = this.bodyBuilder;

        require(bodyBuilder.hasTransactionID(), ".setTransactionId() required");
        require(bodyBuilder.hasNodeAccountID(), ".setNodeAccountId() required");

        doValidate();
        checkValidationErrors("transaction builder failed local validation");
    }

    public final Transaction build(@Nullable Client client) {
        if (client != null && bodyBuilder.getTransactionFee() == 0) {
            setMaxTransactionFee(client.getMaxTransactionFee());
        }

        if (!bodyBuilder.hasNodeAccountID()) {
            Node channel = client != null ? client.pickNode() : null;
            if (channel != null) {
                bodyBuilder.setNodeAccountID(channel.accountId.toProto());
            }
        }

        if (!bodyBuilder.hasTransactionID() && client != null
            && client.getOperatorId() != null)
        {
            bodyBuilder.setTransactionID(new TransactionId(client.getOperatorId()).toProto());
        }

        localValidate();

        inner.setBodyBytes(bodyBuilder.build().toByteString());

        Transaction tx = new Transaction(inner, bodyBuilder, getMethod());

        // Sign with the operator if there is a client; the client has an operator; and, the transaction
        // has a transaction ID that matches that operator ( which it would unless overridden ).
        if (client != null && client.getOperatorKey() != null && client.getOperatorId() != null
            && client.getOperatorId().equals(new AccountId(bodyBuilder.getTransactionID().getAccountID())))
        {
            tx.sign(client.getOperatorKey());
        }

        return tx;
    }

    @Override
    public TransactionId execute(Client client, Duration retryTimeout) throws HederaException, HederaNetworkException {
        return build(client).execute(client, retryTimeout);
    }

    @Override
    public void executeAsync(Client client, Duration retryTimeout, Consumer<TransactionId> onSuccess, Consumer<HederaThrowable> onError) {
        build(client).executeAsync(client, retryTimeout, onSuccess, onError);
    }

    // Work around for java not recognized that this is completely safe
    // as T is required to extend this
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    @Override
    protected Duration getDefaultTimeout() {
        return validDuration;
    }

    @Override
    protected Channel getChannel(Client client) {
        if (bodyBuilder.hasNodeAccountID()) {
            return client.getNodeForId(new AccountId(bodyBuilder.getNodeAccountID())).getChannel();
        } else {
            return client.pickNode().getChannel();
        }
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(
            bodyBuilder.getTransactionIDOrBuilder());
    }
}
