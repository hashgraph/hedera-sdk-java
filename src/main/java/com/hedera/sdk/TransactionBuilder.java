package com.hedera.sdk;

import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.proto.ResponseCodeEnum;
import com.hedera.sdk.proto.TransactionBody;
import com.hedera.sdk.proto.TransactionResponse;
import io.grpc.Channel;

import javax.annotation.Nullable;
import java.time.Duration;
import java.util.Objects;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>>
        extends ValidatingHederaCall<com.hedera.sdk.proto.Transaction, TransactionResponse, ResponseCodeEnum> {
    protected com.hedera.sdk.proto.Transaction.Builder inner = com.hedera.sdk.proto.Transaction.newBuilder();
    protected final TransactionBody.Builder bodyBuilder = inner.getBodyBuilder();

    private static final int MAX_MEMO_LENGTH = 100;

    @Nullable
    protected final Client client;

    // a single required constructor for subclasses so we don't forget
    protected TransactionBuilder(@Nullable Client client) {
        // TODO: map response to non-proto type
        super(TransactionResponse::getNodeTransactionPrecheckCode);
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
        bodyBuilder.setTransactionID(transactionId.inner);
        return self();
    }

    /** Sets the account of the node that submits the transaction to the network. */
    public final T setNodeAccount(AccountId accountId) {
        bodyBuilder.setNodeAccountID(accountId.inner);
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
        bodyBuilder.setTransactionValidDuration(
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
    public final void validate() {
        var bodyBuilder = this.bodyBuilder;
        require(bodyBuilder.hasTransactionID(), ".setTransactionId() required");
        require(bodyBuilder.hasNodeAccountID(), ".setNodeAccount() required");
        doValidate();
        checkValidationErrors("transaction builder failed validation");
    }

    public final com.hedera.sdk.proto.Transaction toProto() {
        return inner.build();
    }

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "TransactionBuilder.client must not be null in normal usage");
        return client.getChannel().getChannel();
    }

    @Override
    protected final com.hedera.sdk.proto.Transaction buildRequest() {
        Objects.requireNonNull(client, "TransactionBuilder.client must not be null in normal usage");

        if (client.getOperatorKey() == null) {
            throw new IllegalStateException("Client.setOperator() is required to implicitly sign transactions");
        }

        return sign(client.getOperatorKey()).toProto();
    }

    public final Transaction sign(Ed25519PrivateKey privateKey) {
        Objects.requireNonNull(client, "TransactionBuilder.client must not be null in normal usage");

        var channel = client.getChannel();

        if (!bodyBuilder.hasNodeAccountID()) {
            bodyBuilder.setNodeAccountID(channel.accountId.toProto());
        }

        if (!bodyBuilder.hasTransactionID() && client.getOperatorId() != null) {
            bodyBuilder.setTransactionID(new TransactionId(client.getOperatorId()).toProto());
        }

        validate();
        return new Transaction(channel.getChannel(), inner, getMethod()).sign(privateKey);
    }

    protected final Transaction testSign(Ed25519PrivateKey privateKey) {
        validate();
        return new Transaction(null, inner, getMethod()).sign(privateKey);
    }

    // Work around for java not recognized that this is completely safe
    // as T is required to extend this
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }
}
