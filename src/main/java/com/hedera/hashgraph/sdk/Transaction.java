package com.hedera.hashgraph.sdk;

import com.google.common.annotations.VisibleForTesting;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.hashgraph.proto.ResponseCodeEnum;
import com.hedera.hashgraph.proto.SignatureMap;
import com.hedera.hashgraph.proto.SignatureMapOrBuilder;
import com.hedera.hashgraph.proto.SignaturePair;
import com.hedera.hashgraph.proto.SignaturePairOrBuilder;
import com.hedera.hashgraph.proto.TransactionBody;
import com.hedera.hashgraph.proto.TransactionBodyOrBuilder;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.FreezeServiceGrpc;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;

import org.bouncycastle.util.encoders.Hex;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public final class Transaction extends HederaCall<com.hedera.hashgraph.proto.Transaction, TransactionResponse, TransactionId, Transaction> {

    static final Duration MAX_VALID_DURATION = Duration.ofMinutes(2);

    private final io.grpc.MethodDescriptor<com.hedera.hashgraph.proto.Transaction, com.hedera.hashgraph.proto.TransactionResponse> methodDescriptor;
    final com.hedera.hashgraph.proto.Transaction.Builder inner;
    final com.hedera.hashgraph.proto.AccountID nodeAccountId;
    final com.hedera.hashgraph.proto.TransactionID txnIdProto;

    @Nullable
    private final Client client;

    // fully qualified to disambiguate
    private final java.time.Duration validDuration;

    private static final int PREFIX_LEN = 6;

    public final TransactionId id;

    Transaction(
        @Nullable Client client,
        com.hedera.hashgraph.proto.Transaction.Builder inner,
        TransactionBodyOrBuilder body,
        MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> methodDescriptor)
    {
        this.client = client;
        this.inner = inner;
        this.nodeAccountId = body.getNodeAccountID();
        this.txnIdProto = body.getTransactionID();
        this.methodDescriptor = methodDescriptor;
        validDuration = DurationHelper.durationTo(body.getTransactionValidDuration());
        id = new TransactionId(txnIdProto);
    }

    /**
     * @deprecated the {@code client} parameter has been moved to {@link #execute(Client)}
     */
    @Deprecated
    public static Transaction fromBytes(Client client, byte[] bytes) throws InvalidProtocolBufferException {
        com.hedera.hashgraph.proto.Transaction inner = com.hedera.hashgraph.proto.Transaction.parseFrom(bytes);
        TransactionBody body = TransactionBody.parseFrom(inner.getBodyBytes());

        return new Transaction(client, inner.toBuilder(), body, methodForTxnBody(body));
    }

    @VisibleForTesting
    public static Transaction fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        com.hedera.hashgraph.proto.Transaction inner = com.hedera.hashgraph.proto.Transaction.parseFrom(bytes);
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

    /**
     * @deprecated renamed to {@link #getReceipt(Client)}
     */
    @Deprecated
    public TransactionReceipt queryReceipt() throws HederaException {
        return getReceipt(Objects.requireNonNull(client));
    }

    /**
     * @deprecated renamed to {@link #getReceipt(Client, Duration)}
     */
    @Deprecated
    public TransactionReceipt queryReceipt(Duration timeout) throws HederaException {
        return getReceipt(Objects.requireNonNull(client), timeout);
    }

    public TransactionReceipt getReceipt(Client client) throws HederaException {
        return id.getReceipt(client);
    }

    public TransactionReceipt getReceipt(Client client, Duration timeout) throws HederaException {
        return id.getReceipt(client, timeout);
    }

    public void getReceiptAsync(Client client, Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        id.getReceiptAsync(client, onReceipt, onError);
    }

    public void getReceiptAsync(Client client, Duration timeout, Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        id.getReceiptAsync(client, timeout, onReceipt, onError);
    }

    public TransactionRecord getRecord(Client client) throws HederaException, HederaNetworkException {
        return id.getRecord(client);
    }

    public TransactionRecord getRecord(Client client, Duration timeout) throws HederaException {
        return id.getRecord(client, timeout);
    }

    public void getRecordAsync(Client client, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        id.getRecordAsync(client, onRecord, onError);
    }

    public void getRecordAsync(Client client, Duration timeout, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        id.getRecordAsync(client, timeout, onRecord, onError);
    }

    @Override
    public com.hedera.hashgraph.proto.Transaction toProto() {
        localValidate();
        return inner.build();
    }

    public com.hedera.hashgraph.proto.Transaction toProto(boolean requireSignature) {
        validate(requireSignature);
        return inner.build();
    }

    @Override
    protected MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "Transaction.client must be non-null in regular use");
        return getChannel(client);
    }

    @Override
    protected Channel getChannel(Client client) {
        Node channel = client.getNodeForId(new AccountId(nodeAccountId));
        Objects.requireNonNull(channel, "Transaction.nodeAccountId not found on Client");

        return channel.getChannel();
    }

    @Override
    public final void localValidate() {
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
            localValidate();
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
     *
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a receipt.
     *
     * Call {@link #execute(Client)} then {@link #getReceipt(Client)} and handle the errors separately from each.
     */
    public final TransactionReceipt executeForReceipt() throws HederaException, HederaNetworkException {
        execute(Objects.requireNonNull(client));
        return getReceipt(client);
    }

    /**
     * Execute this transaction and wait for its record to be generated.
     * <p>
     * If the record does not become available after a few seconds, {@link HederaException} is thrown.
     *
     * @return the receipt for the transaction
     * @throws HederaException        for any response code that is not {@link ResponseCodeEnum#SUCCESS}
     * @throws HederaNetworkException
     * @throws RuntimeException if an {@link java.lang.InterruptedException} is thrown while waiting for the receipt.
     *
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a record.
     *
     * Call {@link #execute(Client)} then {@link #getRecord(Client)} and handle the errors separately from each.
     */
    @Deprecated
    public TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        execute();
        // wait for receipt
        getReceipt(Objects.requireNonNull(client));
        return new TransactionRecordQuery()
            .setTransactionId(id)
            .execute(client);
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a receipt.
     *
     * Call {@link #executeAsync(Client, Consumer, Consumer)} then
     * {@link #getReceiptAsync(Client, Consumer, Consumer)} and handle the errors separately
     * from each.
     */
    @Deprecated
    public void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        final Client client = Objects.requireNonNull(this.client);
        executeAsync(client, id -> getReceiptAsync(client, onSuccess, onError), onError);
    }

    /**
     * Equivalent to {@link #executeForReceiptAsync(Consumer, Consumer)} but providing {@code this}
     * to the callback for additional context.
     *
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a receipt.
     *
     * Call {@link #executeAsync(Client, Consumer, Consumer)} then
     * {@link #getReceiptAsync(Client, Consumer, Consumer)} and handle the errors
     * separately from each.
     */
    @Deprecated
    public final void executeForReceiptAsync(BiConsumer<Transaction, TransactionReceipt> onSuccess, BiConsumer<Transaction, HederaThrowable> onError) {
        executeForReceiptAsync(r -> onSuccess.accept(this, r), e -> onError.accept(this, e));
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a record.
     *
     * Call {@link #executeAsync(Client, Consumer, Consumer)} then
     * {@link #getRecordAsync(Client, Consumer, Consumer)} and handle the errors separately
     * from each.
     */
    @Deprecated
    public void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<HederaThrowable> onError) {
        final TransactionRecordQuery recordQuery = new TransactionRecordQuery(Objects.requireNonNull(client))
            .setTransactionId(id);

        executeForReceiptAsync((receipt) -> recordQuery.executeAsync(onSuccess, onError), onError);
    }

    /**
     * @deprecated Makes it difficult to discern whether an error occurred while executing or
     * while fetching a record.
     *
     * Call {@link #executeAsync(Client, Consumer, Consumer)} then
     * {@link #getRecordAsync(Client, Consumer, Consumer)} and handle the errors separately
     * from each.
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

    private static ByteString getPrefix(ByteString byteString) {
        if (byteString.size() <= PREFIX_LEN) {
            return byteString;
        }

        return byteString.substring(0, PREFIX_LEN);
    }

    private static MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> methodForTxnBody(TransactionBodyOrBuilder body) {
        switch (body.getDataCase()) {
            // System

            case SYSTEMDELETE:
                return FileServiceGrpc.getSystemDeleteMethod();
            case SYSTEMUNDELETE:
                return FileServiceGrpc.getSystemUndeleteMethod();
            case FREEZE:
                return FreezeServiceGrpc.getFreezeMethod();

            // Contracts

            case CONTRACTCALL:
                return SmartContractServiceGrpc.getContractCallMethodMethod();
            case CONTRACTCREATEINSTANCE:
                return SmartContractServiceGrpc.getCreateContractMethod();
            case CONTRACTUPDATEINSTANCE:
                return SmartContractServiceGrpc.getUpdateContractMethod();
            case CONTRACTDELETEINSTANCE:
                return SmartContractServiceGrpc.getDeleteContractMethod();

            // Account / Crypto

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

            // Files

            case FILEAPPEND:
                return FileServiceGrpc.getAppendContentMethod();
            case FILECREATE:
                return FileServiceGrpc.getCreateFileMethod();
            case FILEDELETE:
                return FileServiceGrpc.getDeleteFileMethod();
            case FILEUPDATE:
                return FileServiceGrpc.getUpdateFileMethod();

            // Consensus

            case CONSENSUSCREATETOPIC:
                return ConsensusServiceGrpc.getCreateTopicMethod();
            case CONSENSUSUPDATETOPIC:
                return ConsensusServiceGrpc.getUpdateTopicMethod();
            case CONSENSUSDELETETOPIC:
                return ConsensusServiceGrpc.getDeleteTopicMethod();

            case CONSENSUSSUBMITMESSAGE:
                return ConsensusServiceGrpc.getSubmitMessageMethod();

            case DATA_NOT_SET:
                throw new IllegalArgumentException("method not set");

            default:
                throw new IllegalArgumentException("unsupported method");
        }
    }
}
