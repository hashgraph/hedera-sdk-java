package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionResponse;

import java.time.Duration;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>>
    extends HederaCall<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse, TransactionId, T>
{
    protected final com.hederahashgraph.api.proto.java.Transaction.Builder inner = com.hederahashgraph.api.proto.java.Transaction.newBuilder();
    protected final TransactionBody.Builder bodyBuilder = TransactionBody.newBuilder();

    {
        setTransactionValidDuration(Transaction.MAX_VALID_DURATION);
    }

    private static final int MAX_MEMO_LENGTH = 100;

    private Duration validDuration = Transaction.MAX_VALID_DURATION;

    @Nullable
    protected final Client client;

    // a single required constructor for subclasses so we don't forget
    protected TransactionBuilder(@Nullable Client client) {
        super();
        this.client = client;
    }

    public TransactionBuilder() {
        this(null);
    }

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
     * Sets the fee that the client pays to execute this transaction, which is split between the
     * network and the node.
     *
     * @deprecated renamed to {@link #setMaxTransactionFee(long)} (the semantics are unchanged)
     */
    @Deprecated
    public final T setTransactionFee(long fee) {
        return setMaxTransactionFee(fee);
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
     * Sets whether the transaction should generate a longer-lived record at the cost of a higher transaction fee.
     * Records by default only live a few minutes but setting this causes them to persist for an hour.
     */
    public final T setGenerateRecord(boolean generateRecord) {
        bodyBuilder.setGenerateRecord(generateRecord);
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

        bodyBuilder.setMemo(memo);
        return self();
    }

    protected abstract void doValidate();

    @Override
    public final com.hederahashgraph.api.proto.java.Transaction toProto() {
        return build().toProto();
    }

    public final com.hederahashgraph.api.proto.java.Transaction toProto(boolean requireSignature) {
        return build().toProto(requireSignature);
    }

    @Override
    protected final void localValidate() {
        TransactionBody.Builder bodyBuilder = this.bodyBuilder;

        if (client == null) {
            require(bodyBuilder.hasTransactionID(), ".setTransactionId() required");
        }

        if (client == null || client.getOperatorId() == null) {
            require(bodyBuilder.hasNodeAccountID(), ".setNodeAccountId() required");
        }

        doValidate();
        checkValidationErrors("transaction builder failed local validation");
    }

    public final Transaction build() {
        return build(client);
    }

    public final Transaction build(@Nullable Client client) {
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

        inner.setBodyBytes(
            bodyBuilder.build()
                .toByteString());
        Transaction tx = new Transaction(null, inner, bodyBuilder, getMethod());

        if (client != null && client.getOperatorKey() != null) {
            tx.sign(client.getOperatorKey());
        }

        return tx;
    }

    /**
     * @deprecated use {@code .build().sign()} instead.
     */
    @Deprecated
    public final Transaction sign(Ed25519PrivateKey privateKey) {
        return build().sign(privateKey);
    }

    /**
     * @deprecated use {@code .build().toBytes()} instead.
     */
    @Deprecated
    public final byte[] toBytes() {
        return build().toBytes();
    }

    /**
     * @deprecated use {@code .build().toBytes()} instead.
     */
    @Deprecated
    public final byte[] toBytes(boolean requiresSignature) {
        return build().toBytes(requiresSignature);
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

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a receipt. If an error occurs while fetching the receipt then the transaction
     * ID is difficult to retrieve.
     * <p>
     * Call {@link #build()} then {@link Transaction#execute()} then
     * {@link Transaction#getReceipt()} and handle the errors separately from each.
     */
    @Deprecated
    public final TransactionReceipt executeForReceipt() throws HederaException, HederaNetworkException {
        return build().executeForReceipt();
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a receipt. If an error occurs while fetching the receipt then the transaction
     * ID is difficult to retrieve.
     * <p>
     * Call {@link #build()} then {@link Transaction#executeAsync(Consumer, Consumer)} then
     * {@link Transaction#getReceiptAsync(Consumer, Consumer)} and handle the errors separately
     * from each.
     */
    @Deprecated
    public final void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        build().executeForReceiptAsync(onSuccess, onError);
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a receipt. If an error occurs while fetching the receipt then the transaction
     * ID is difficult to retrieve.
     */
    @Deprecated
    public final void executeForReceiptAsync(BiConsumer<T, TransactionReceipt> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        //noinspection unchecked
        build().executeForReceiptAsync(r -> onSuccess.accept((T) this, r), e -> onError.accept((T) this, e));
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a record. If an error occurs while fetching the record then the transaction
     * ID is difficult to retrieve.
     * <p>
     * Call {@link #build()} then {@link Transaction#execute()} then
     * {@link Transaction#getRecord()} and handle the errors separately from each.
     */
    @Deprecated
    public final TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        return build().executeForRecord();
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a record. If an error occurs while fetching the record then the transaction
     * ID is difficult to retrieve.
     * <p>
     * Call {@link #build()} then {@link Transaction#executeAsync(Consumer, Consumer)} then
     * {@link Transaction#getRecordAsync(Consumer, Consumer)} and handle the errors separately
     * from each.
     */
    @Deprecated
    public final void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<HederaThrowable> onError) {
        build().executeForRecordAsync(onSuccess, onError);
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a record. If an error occurs while fetching the record then the transaction
     * ID is difficult to retrieve.
     */
    @Deprecated
    public final void executeForRecordAsync(BiConsumer<T, TransactionRecord> onSuccess, BiConsumer<T, HederaThrowable> onError) {
        //noinspection unchecked
        build().executeForRecordAsync(r -> onSuccess.accept((T) this, r), e -> onError.accept((T) this, e));
    }

    // FIXME: This is duplicated from Transaction

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "TransactionBuilder.client must not be null in normal usage");
        return getChannel(client);
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
