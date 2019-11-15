package com.hedera.hashgraph.sdk;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519Signature;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import com.hederahashgraph.api.proto.java.SignatureMap;
import com.hederahashgraph.api.proto.java.SignatureMapOrBuilder;
import com.hederahashgraph.api.proto.java.SignaturePair;
import com.hederahashgraph.api.proto.java.SignaturePairOrBuilder;
import com.hederahashgraph.api.proto.java.TransactionBody;
import com.hederahashgraph.api.proto.java.TransactionBodyOrBuilder;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;
import com.hederahashgraph.service.proto.java.FileServiceGrpc;
import com.hederahashgraph.service.proto.java.SmartContractServiceGrpc;

import org.bouncycastle.util.encoders.Hex;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public final class Transaction extends HederaCall<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse, TransactionId, Transaction> {

    static final Duration MAX_VALID_DURATION = Duration.ofMinutes(2);

    private final io.grpc.MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, com.hederahashgraph.api.proto.java.TransactionResponse> methodDescriptor;
    final com.hederahashgraph.api.proto.java.Transaction.Builder inner;
    final com.hederahashgraph.api.proto.java.AccountID nodeAccountId;
    final com.hederahashgraph.api.proto.java.TransactionID txnIdProto;

    @Nullable
    private final Client client;

    // fully qualified to disambiguate
    private final java.time.Duration validDuration;

    private static final int PREFIX_LEN = 6;

    public final TransactionId id;

    Transaction(
        @Nullable Client client,
        com.hederahashgraph.api.proto.java.Transaction.Builder inner,
        TransactionBodyOrBuilder body,
        MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse> methodDescriptor)
    {
        this.client = client;
        this.inner = inner;
        this.nodeAccountId = body.getNodeAccountID();
        this.txnIdProto = body.getTransactionID();
        this.methodDescriptor = methodDescriptor;
        validDuration = DurationHelper.durationTo(body.getTransactionValidDuration());
        id = new TransactionId(txnIdProto);
    }

    public static Transaction fromBytes(Client client, byte[] bytes) throws InvalidProtocolBufferException {
        com.hederahashgraph.api.proto.java.Transaction inner = com.hederahashgraph.api.proto.java.Transaction.parseFrom(bytes);
        TransactionBody body = TransactionBody.parseFrom(inner.getBodyBytes());

        return new Transaction(client, inner.toBuilder(), body, methodForTxnBody(body));
    }

    @VisibleForTesting
    public static Transaction fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        com.hederahashgraph.api.proto.java.Transaction inner = com.hederahashgraph.api.proto.java.Transaction.parseFrom(bytes);
        TransactionBody body = TransactionBody.parseFrom(inner.getBodyBytes());

        return new Transaction(null, inner.toBuilder(), body, methodForTxnBody(body));
    }

    public Transaction sign(Ed25519PrivateKey privateKey) {
        ByteString pubKey = ByteString.copyFrom(
            privateKey.getPublicKey()
                .toBytes());

        SignatureMap.Builder sigMap = inner.getSigMapBuilder();

        for (int i = 0; i < sigMap.getSigPairCount(); i++) {
            ByteString pubKeyPrefix = sigMap.getSigPair(i)
                .getPubKeyPrefix();

            if (pubKey.startsWith(pubKeyPrefix)) {
                throw new IllegalArgumentException(
                    "transaction already signed with key: " + privateKey.toString());
            }
        }

        byte[] signature = Ed25519Signature.forMessage(
            privateKey,
            inner.getBodyBytes()
                .toByteArray())
            .toBytes();

        sigMap.addSigPair(
            SignaturePair.newBuilder()
                .setPubKeyPrefix(pubKey)
                .setEd25519(ByteString.copyFrom(signature))
                .build());

        return this;
    }

    /**
     * @deprecated now available as {{@link #id}} instead.
     */
    @Deprecated
    public TransactionId getId() {
        return new TransactionId(txnIdProto);
    }

    public TransactionReceipt queryReceipt() throws HederaException {
        return new TransactionReceiptQuery(Objects.requireNonNull(client))
            .setTransactionId(id)
            .execute();
    }

    public void queryReceiptAsync(Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        new TransactionReceiptQuery(Objects.requireNonNull(client))
            .setTransactionId(id)
            .executeAsync(onReceipt, onError);
    }

    @Override
    public com.hederahashgraph.api.proto.java.Transaction toProto() {
        validate();
        return inner.build();
    }

    public com.hederahashgraph.api.proto.java.Transaction toProto(boolean requireSignature) {
        validate(requireSignature);
        return inner.build();
    }

    @Override
    protected MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "Transaction.client must be non-null in regular use");

        Node channel = client.getNodeForId(new AccountId(nodeAccountId));
        Objects.requireNonNull(channel, "Transaction.nodeAccountId not found on Client");

        return channel.getChannel();
    }

    @Override
    public final void validate() {
        SignatureMapOrBuilder sigMap = inner.getSigMapOrBuilder();

        if (sigMap.getSigPairCount() < 2) {
            if (sigMap.getSigPairCount() == 0) {
                addValidationError("Transaction requires at least one signature");
            } // else contains one signature which is fine
        } else {
            HashSet<Object> publicKeys = new HashSet<>();

            for (int i = 0; i < sigMap.getSigPairCount(); i++) {
                SignaturePairOrBuilder sig = sigMap.getSigPairOrBuilder(i);
                ByteString pubKeyPrefix = sig.getPubKeyPrefix();

                if (!publicKeys.add(pubKeyPrefix)) {
                    addValidationError("duplicate signing key: "
                        + Hex.toHexString(getPrefix(pubKeyPrefix).toByteArray()) + "...");
                }
            }
        }

        checkValidationErrors("Transaction failed validation");
    }

    protected void validate(boolean requireSignature) {
        if (requireSignature) {
            validate();
            return;
        }

        checkValidationErrors("Transaction failed validation");
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaException {
        HederaException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(txnIdProto);
    }

    @Override
    protected Duration getDefaultTimeout() {
        return validDuration;
    }

    /**
     * Execute this transaction and wait for its receipt to be generated.
     * <p>
     * If the receipt does not become available after a few seconds, {@link HederaException} is thrown.
     *
     * @return the receipt for the transaction
     * @throws HederaException        for any response code that is not {@link ResponseCodeEnum#SUCCESS}
     * @throws HederaNetworkException
     * @throws RuntimeException       if an {@link java.lang.InterruptedException} is thrown while waiting for the receipt.
     */
    public final TransactionReceipt executeForReceipt() throws HederaException, HederaNetworkException {
        execute();
        return queryReceipt();
    }

    /**
     * Execute this transaction and wait for its record to be generated.
     * <p>
     * If the record does not become available after a few seconds, {@link HederaException} is thrown.
     *
     * @return the receipt for the transaction
     * @throws HederaException        for any response code that is not {@link ResponseCodeEnum#SUCCESS}
     * @throws HederaNetworkException
     * @throws RuntimeException       if an {@link java.lang.InterruptedException} is thrown while waiting for the receipt.
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     */
    @Deprecated
    public TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        execute();
        // wait for receipt
        queryReceipt();
        return new TransactionRecordQuery(Objects.requireNonNull(this.client))
            .setTransactionId(id)
            .execute();
    }

    public void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        executeAsync(id -> queryReceiptAsync(onSuccess, onError), onError);
    }

    /**
     * Equivalent to {@link #executeForReceiptAsync(Consumer, Consumer)} but providing {@code this}
     * to the callback for additional context.
     */
    public final void executeForReceiptAsync(BiConsumer<Transaction, TransactionReceipt> onSuccess, BiConsumer<Transaction, HederaThrowable> onError) {
        executeForReceiptAsync(r -> onSuccess.accept(this, r), e -> onError.accept(this, e));
    }

    /**
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     */
    @Deprecated
    public void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<HederaThrowable> onError) {
        final TransactionRecordQuery recordQuery = new TransactionRecordQuery(getClient())
            .setTransactionId(id);

        executeForReceiptAsync((receipt) -> recordQuery.executeAsync(onSuccess, onError), onError);
    }

    /**
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     */
    @Deprecated
    public final void executeForRecordAsync(BiConsumer<Transaction, TransactionRecord> onSuccess, BiConsumer<Transaction, HederaThrowable> onError) {
        executeForRecordAsync(r -> onSuccess.accept(this, r), e -> onError.accept(this, e));
    }

    public byte[] toBytes() {
        return toProto().toByteArray();
    }

    public byte[] toBytes(boolean requiresSignature) {
        return toProto(requiresSignature).toByteArray();
    }

    private Client getClient() {
        return Objects.requireNonNull(client);
    }

    private static ByteString getPrefix(ByteString byteString) {
        if (byteString.size() <= PREFIX_LEN) {
            return byteString;
        }

        return byteString.substring(0, PREFIX_LEN);
    }

    private static MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse> methodForTxnBody(TransactionBodyOrBuilder body) {
        switch (body.getDataCase()) {
            case SYSTEMDELETE:
                return FileServiceGrpc.getSystemDeleteMethod();
            case SYSTEMUNDELETE:
                return FileServiceGrpc.getSystemUndeleteMethod();
            case CONTRACTCALL:
                return SmartContractServiceGrpc.getContractCallMethodMethod();
            case CONTRACTCREATEINSTANCE:
                return SmartContractServiceGrpc.getCreateContractMethod();
            case CONTRACTUPDATEINSTANCE:
                return SmartContractServiceGrpc.getUpdateContractMethod();
            case CONTRACTDELETEINSTANCE:
                return SmartContractServiceGrpc.getDeleteContractMethod();
            case CRYPTOADDCLAIM:
                return CryptoServiceGrpc.getAddClaimMethod();
            case CRYPTOCREATEACCOUNT:
                return CryptoServiceGrpc.getCreateAccountMethod();
            case CRYPTODELETE:
                return CryptoServiceGrpc.getCryptoDeleteMethod();
            case CRYPTODELETECLAIM:
                return CryptoServiceGrpc.getDeleteClaimMethod();
            case CRYPTOTRANSFER:
                return CryptoServiceGrpc.getCryptoTransferMethod();
            case CRYPTOUPDATEACCOUNT:
                return CryptoServiceGrpc.getUpdateAccountMethod();
            case FILEAPPEND:
                return FileServiceGrpc.getAppendContentMethod();
            case FILECREATE:
                return FileServiceGrpc.getCreateFileMethod();
            case FILEDELETE:
                return FileServiceGrpc.getDeleteFileMethod();
            case FILEUPDATE:
                return FileServiceGrpc.getUpdateFileMethod();
            case DATA_NOT_SET:
                throw new IllegalArgumentException("method not set");
            default:
                throw new IllegalArgumentException("unsupported method");
        }
    }
}
