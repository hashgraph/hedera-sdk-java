package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519Signature;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;
import com.hedera.hashgraph.sdk.proto.SignaturePair;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBodyOrBuilder;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;

import org.bouncycastle.util.encoders.Hex;

import java.util.HashSet;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.netty.shaded.io.netty.util.concurrent.GlobalEventExecutor;

public final class Transaction extends HederaCall<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse, TransactionId> {

    private final io.grpc.MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, com.hedera.hashgraph.sdk.proto.TransactionResponse> methodDescriptor;
    final com.hedera.hashgraph.sdk.proto.Transaction.Builder inner;
    final com.hedera.hashgraph.sdk.proto.AccountID nodeAccountId;
    final com.hedera.hashgraph.sdk.proto.TransactionID transactionId;

    @Nullable
    private final Client client;

    private static final int MAX_RETRY_ATTEMPTS = 100;
    private static final int RECEIPT_RETRY_DELAY = 100;

    private static final int PREFIX_LEN = 6;

    Transaction(
        @Nullable Client client,
        com.hedera.hashgraph.sdk.proto.Transaction.Builder inner,
        com.hedera.hashgraph.sdk.proto.AccountID nodeAccountId,
        com.hedera.hashgraph.sdk.proto.TransactionID transactionId,
        MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> methodDescriptor)
    {
        super();
        this.client = client;
        this.inner = inner;
        this.nodeAccountId = nodeAccountId;
        this.transactionId = transactionId;
        this.methodDescriptor = methodDescriptor;
    }

    public static Transaction fromBytes(Client client, byte[] bytes) throws InvalidProtocolBufferException {
        var inner = com.hedera.hashgraph.sdk.proto.Transaction.parseFrom(bytes);
        var body = TransactionBody.parseFrom(inner.getBodyBytes());

        return new Transaction(client, inner.toBuilder(), body.getNodeAccountID(), body.getTransactionID(), methodForTxnBody(body));
    }

    public Transaction sign(Ed25519PrivateKey privateKey) {
        var pubKey = ByteString.copyFrom(
            privateKey.getPublicKey()
                .toBytes());

        var sigMap = inner.getSigMapBuilder();

        for (int i = 0; i < sigMap.getSigPairCount(); i++) {
            var pubKeyPrefix = sigMap.getSigPair(i)
                .getPubKeyPrefix();

            if (pubKey.startsWith(pubKeyPrefix)) {
                throw new IllegalArgumentException("transaction already signed with key: " + privateKey.toString());
            }
        }

        var signature = Ed25519Signature.forMessage(
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
    public com.hedera.hashgraph.sdk.proto.Transaction toProto() {
        validate();
        return inner.build();
    }

    public com.hedera.hashgraph.sdk.proto.Transaction toProto(boolean requireSignature) {
        validate(requireSignature);
        return inner.build();
    }

    @Override
    protected MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    @Override
    protected Channel getChannel() {
        Objects.requireNonNull(client, "Transaction.client must be non-null in regular use");

        var channel = client.getNodeForId(new AccountId(nodeAccountId));
        Objects.requireNonNull(channel, "Transaction.nodeAccountId not found on Client");

        return channel.getChannel();
    }

    @Override
    public final void validate() {
        var sigMap = inner.getSigMapOrBuilder();

        if (sigMap.getSigPairCount() < 2) {
            if (sigMap.getSigPairCount() == 0) {
                addValidationError("Transaction requires at least one signature");
            } // else contains one signature which is fine
        } else {
            var publicKeys = new HashSet<>();

            for (int i = 0; i < sigMap.getSigPairCount(); i++) {
                var sig = sigMap.getSigPairOrBuilder(i);
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
        // kickoff the transaction
        execute();

        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            var receipt = queryReceipt();
            var receiptStatus = receipt.getStatus();

            // if we're fetching a record it returns `RECORD_NOT_FOUND` instead of `UNKNOWN`
            if (receiptStatus == ResponseCodeEnum.UNKNOWN) {
                // If the receipt is UNKNOWN this means that the server has not finished
                // processing the transaction
                try {
                    Thread.sleep(RECEIPT_RETRY_DELAY * attempt);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                HederaException.throwIfExceptional(receiptStatus);
                return mapReceipt.apply(receipt);
            }
        }

        throw new HederaException(ResponseCodeEnum.UNKNOWN);
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
     */
    public TransactionRecord executeForRecord() throws HederaException, HederaNetworkException {
        return executeAndWaitFor(
            receipt -> new TransactionRecordQuery(getClient()).setTransactionId(getId()).execute());
    }

    public void executeForReceiptAsync(Consumer<TransactionReceipt> onSuccess, Consumer<HederaThrowable> onError) {
        var handler = new AsyncReceiptHandler(onSuccess, onError);

        executeAsync(id -> handler.tryGetReceipt(), onError);
    }

    public void executeForRecordAsync(Consumer<TransactionRecord> onSuccess, Consumer<HederaThrowable> onError) {
        final var recordQuery = new TransactionRecordQuery(getClient())
            .setTransactionId(getId())
            .setPaymentDefault();

        var handler = new AsyncReceiptHandler((receipt) ->
            recordQuery.executeAsync(onSuccess, onError),
            onError);

        executeAsync(id -> handler.tryGetReceipt(), onError);
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

    private static MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> methodForTxnBody(TransactionBodyOrBuilder body) {
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

        private int attemptsLeft = MAX_RETRY_ATTEMPTS;
        private volatile boolean receiptEmitted = false;

        private AsyncReceiptHandler(
            Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError)
        {
            this.onReceipt = onReceipt;
            this.onError = onError;
        }

        private void tryGetReceipt() {
            attemptsLeft -= 1;
            queryReceiptAsync(this::handleReceipt, onError);
        }

        private void handleReceipt(TransactionReceipt receipt) {
            if (receipt.getStatus() == ResponseCodeEnum.UNKNOWN) {
                if (attemptsLeft == 0) {
                    onError.accept(new HederaException(ResponseCodeEnum.UNKNOWN));
                } else {
                    GlobalEventExecutor.INSTANCE.schedule(this::tryGetReceipt, RECEIPT_RETRY_DELAY,
                        TimeUnit.MILLISECONDS);
                }
            } else {
                if (receiptEmitted) throw new IllegalStateException();
                receiptEmitted = true;

                onReceipt.accept(receipt);
            }
        }
    }
}
