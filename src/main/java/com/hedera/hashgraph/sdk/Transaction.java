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

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.netty.shaded.io.netty.util.concurrent.GlobalEventExecutor;

public final class Transaction extends HederaCall<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse, TransactionId, Transaction> {

    private final io.grpc.MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, com.hederahashgraph.api.proto.java.TransactionResponse> methodDescriptor;
    final com.hederahashgraph.api.proto.java.Transaction.Builder inner;
    final com.hederahashgraph.api.proto.java.AccountID nodeAccountId;
    final com.hederahashgraph.api.proto.java.TransactionID transactionId;

    @Nullable
    private final Client client;

    // fully qualified to disambiguate
    private final java.time.Duration validDuration;

    private static final int RECEIPT_RETRY_DELAY = 500;
    private static final int RECEIPT_INITIAL_DELAY = 1000;

    private static final int PREFIX_LEN = 6;

    Transaction(
        @Nullable Client client,
        com.hederahashgraph.api.proto.java.Transaction.Builder inner,
        TransactionBodyOrBuilder body,
        MethodDescriptor<com.hederahashgraph.api.proto.java.Transaction, TransactionResponse> methodDescriptor)
    {
        super();
        this.client = client;
        this.inner = inner;
        this.nodeAccountId = body.getNodeAccountID();
        this.transactionId = body.getTransactionID();
        this.methodDescriptor = methodDescriptor;
        validDuration = DurationHelper.durationTo(body.getTransactionValidDuration());
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
                throw new IllegalArgumentException("transaction already signed with key: " + privateKey.toString());
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

    public TransactionId getId() {
        return new TransactionId(transactionId);
    }

    public TransactionReceipt queryReceipt() throws HederaException {
        return new TransactionReceiptQuery(Objects.requireNonNull(client))
            .setTransactionId(getId())
            .execute();
    }

    public void queryReceiptAsync(Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        new TransactionReceiptQuery(Objects.requireNonNull(client))
            .setTransactionId(getId())
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
                    addValidationError("duplicate signing key: " + Hex.toHexString(getPrefix(pubKeyPrefix).toByteArray()) + "...");
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
        return new TransactionId(transactionId);
    }

    private <T> T executeAndWaitFor(CheckedFunction<TransactionReceipt, T> mapReceipt)
        throws HederaException, HederaNetworkException
    {
        final Instant startTime = Instant.now();

        // kickoff the transaction
        execute();

        try {
            Thread.sleep(RECEIPT_INITIAL_DELAY);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        int attempt = 0;

        while (true) {
            attempt += 1;
            TransactionReceipt receipt = queryReceipt();
            ResponseCodeEnum receiptStatus = receipt.getStatus();

            if (receiptStatus == ResponseCodeEnum.UNKNOWN || receiptStatus == ResponseCodeEnum.OK) {
                // If the receipt is UNKNOWN this means that the node has not finished
                // processing the transaction; if it is OK it means the transaction has not yet
                // reached consensus

                final Long delayMs =
                    getReceiptDelayMs(startTime, attempt)
                        // throw if the delay will put us over `validDuraiton`
                        .orElseThrow(() -> new HederaException(ResponseCodeEnum.UNKNOWN));

                try {
                    Thread.sleep(delayMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                HederaException.throwIfExceptional(receiptStatus);
                return mapReceipt.apply(receipt);
            }
        }
    }

    private Optional<Long> getReceiptDelayMs(Instant startTime, int attempt) {
        // exponential backoff algorithm:
        // next delay is some constant * rand(0, 2 ** attempt - 1)
        final int delay = RECEIPT_RETRY_DELAY
            * Objects.requireNonNull(client).random.nextInt(1 << attempt);

        // if the next delay will put us past the valid duration throw an error
        if (Instant.now().plusMillis(delay).compareTo(startTime.plus(validDuration)) > 0) {
            return Optional.empty();
        }

        return Optional.of((long) delay);
    }

    /**
     * Execute this transaction and wait for its receipt to be generated.
     *
     * If the receipt does not become available after a few seconds, {@link HederaException} is thrown.
     *
     * @return the receipt for the transaction
     * @throws HederaException for any response code that is not {@link ResponseCodeEnum#SUCCESS}
     * @throws HederaNetworkException
     * @throws RuntimeException if an {@link java.lang.InterruptedException} is thrown while waiting for the receipt.
     */
    public final TransactionReceipt executeForReceipt() throws HederaException, HederaNetworkException {
        return executeAndWaitFor(receipt -> receipt);
    }

    /**
     * Execute this transaction and wait for its record to be generated.
     *
     * If the record does not become available after a few seconds, {@link HederaException} is thrown.
     *
     * @return the receipt for the transaction
     * @throws HederaException for any response code that is not {@link ResponseCodeEnum#SUCCESS}
     * @throws HederaNetworkException
     * @throws RuntimeException if an {@link java.lang.InterruptedException} is thrown while waiting for the receipt.
     * @deprecated querying for records has a cost separate from executing the transaction and so
     * should be done in an explicit step
     *
     */
    @Deprecated
    public TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        return executeAndWaitFor(
            receipt -> new TransactionRecordQuery(getClient()).setTransactionId(getId()).execute());
    }

    public void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        AsyncReceiptHandler handler = new AsyncReceiptHandler(onSuccess, onError);

        executeAsync(id -> handler.tryGetReceipt(), onError);
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
            .setTransactionId(getId());

        AsyncReceiptHandler handler = new AsyncReceiptHandler((receipt) ->
            recordQuery.executeAsync(onSuccess, onError),
            onError);

        executeAsync(id -> handler.tryGetReceipt(), onError);
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

    private final class AsyncReceiptHandler {
        private final Consumer<TransactionReceipt> onReceipt;
        private final Consumer<HederaThrowable> onError;

        // sentinel value signaling initial delay needs to be scheduled
        private int attempts = -1;
        private volatile boolean receiptEmitted = false;

        private final Instant startTime = Instant.now();

        private AsyncReceiptHandler(
            Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError)
        {
            this.onReceipt = onReceipt;
            this.onError = onError;
        }

        private void tryGetReceipt() {
            if (attempts == -1) {
                scheduleReceiptAttempt(RECEIPT_INITIAL_DELAY);
                attempts = 0;
            } else {
                queryReceiptAsync(this::handleReceipt, onError);
            }
        }

        private void handleReceipt(TransactionReceipt receipt) {
            final ResponseCodeEnum status = receipt.getStatus();

            if (status == ResponseCodeEnum.UNKNOWN || status == ResponseCodeEnum.OK) {
                final Optional<Long> receiptDelayMs = getReceiptDelayMs(startTime, ++attempts); // increment and get
                receiptDelayMs.ifPresent(this::scheduleReceiptAttempt);

                if (!receiptDelayMs.isPresent()) {
                    onError.accept(new HederaException(ResponseCodeEnum.UNKNOWN));
                }
            } else {
                if (receiptEmitted) throw new IllegalStateException();
                receiptEmitted = true;

                onReceipt.accept(receipt);
            }
        }

        private void scheduleReceiptAttempt(long delayMs) {
            GlobalEventExecutor.INSTANCE.schedule(this::tryGetReceipt, delayMs,
                TimeUnit.MILLISECONDS);
        }
    }
}
