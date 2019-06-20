package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.Channel;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>>
        extends HederaCall<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse, TransactionId>
{
    protected final com.hedera.hashgraph.sdk.proto.Transaction.Builder inner = com.hedera.hashgraph.sdk.proto.Transaction.newBuilder();
    protected final TransactionBody.Builder bodyBuilder = TransactionBody.newBuilder();

    private static final int MAX_MEMO_LENGTH = 100;

    @Nullable
    protected final Client client;

    // a single required constructor for subclasses so we don't forget
    protected TransactionBuilder(@Nullable Client client) {
        super();
        this.client = client;

        setTransactionFee(client != null ? client.getMaxTransactionFee() : Client.DEFAULT_MAX_TXN_FEE);
        setTransactionValidDuration(Duration.ofMinutes(2));
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

    /** Sets the account of the node that submits the transaction to the network. */
    public final T setNodeAccountId(AccountId accountId) {
        bodyBuilder.setNodeAccountID(accountId.toProto());
        return self();
    }

    /**
     * Sets the fee that the client pays to execute this transaction, which is split between the
     * network and the node.
     */
    public final T setTransactionFee(long fee) {
        bodyBuilder.setTransactionFee(fee);
        return self();
    }

    /**
     * Sets the the duration that this transaction is valid for. The transaction must consensus
     * before this this elapses.
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        bodyBuilder.setTransactionValidDuration(DurationHelper.durationFrom(validDuration));

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
    public final com.hedera.hashgraph.sdk.proto.Transaction toProto() {
        return build().toProto();
    }

    public final com.hedera.hashgraph.sdk.proto.Transaction toProto(boolean requireSignature) {
        return build().toProto(requireSignature);
    }

    @Override
    public final void validate() {
        var bodyBuilder = this.bodyBuilder;

        if (client == null) {
            require(bodyBuilder.hasTransactionID(), ".setTransactionId() required");
        }

        if (client == null || client.getOperatorId() == null) {
            require(bodyBuilder.hasNodeAccountID(), ".setNodeAccountId() required");
        }

        doValidate();
        checkValidationErrors("transaction builder failed validation");
    }

    public final Transaction build() {
        if (!bodyBuilder.hasNodeAccountID()) {
            var channel = client == null ? null : client.pickNode();
            if (channel != null) {
                bodyBuilder.setNodeAccountID(channel.accountId.toProto());
            }
        }

        if (!bodyBuilder.hasTransactionID() && client != null && client.getOperatorId() != null) {
            bodyBuilder.setTransactionID(new TransactionId(client.getOperatorId()).toProto());
        }

        validate();

        inner.setBodyBytes(
            bodyBuilder.build()
                .toByteString());
        var tx = new Transaction(client, inner, bodyBuilder.getNodeAccountID(), bodyBuilder.getTransactionID(), getMethod());

        if (client != null && client.getOperatorKey() != null) {
            tx.sign(client.getOperatorKey());
        }

        return tx;
    }

    public final Transaction sign(Ed25519PrivateKey privateKey) {
        return build().sign(privateKey);
    }

    public final byte[] toBytes() {
        return build().toBytes();
    }

    public final byte[] toBytes(boolean requiresSignature) {
        return build().toBytes(requiresSignature);
    }

    // Work around for java not recognized that this is completely safe
    // as T is required to extend this
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    public final TransactionReceipt executeForReceipt() throws HederaException, HederaNetworkException {
        return build().executeForReceipt();
    }

    public final void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        build().executeForReceiptAsync(onSuccess, onError);
    }

    public final TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        return build().executeForRecord();
    }

    public final void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<HederaThrowable> onError) {
        build().executeForRecordAsync(onSuccess, onError);
    }

    // FIXME: This is duplicated from Transaction

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "TransactionBuilder.client must not be null in normal usage");
        return client.pickNode()
            .getChannel();
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(
                bodyBuilder.getTransactionIDOrBuilder());
    }
}
