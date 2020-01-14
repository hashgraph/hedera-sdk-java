package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.FreezeServiceGrpc;
import com.hedera.hashgraph.proto.SignatureMap;
import com.hedera.hashgraph.proto.SignatureMapOrBuilder;
import com.hedera.hashgraph.proto.SignaturePair;
import com.hedera.hashgraph.proto.SignaturePairOrBuilder;
import com.hedera.hashgraph.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.proto.TransactionBody;
import com.hedera.hashgraph.proto.TransactionBodyOrBuilder;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import org.bouncycastle.util.encoders.Hex;

import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

import io.grpc.Channel;
import io.grpc.MethodDescriptor;

public final class Transaction extends HederaCall<com.hedera.hashgraph.proto.Transaction, TransactionResponse, TransactionId, Transaction> {

    static final Duration MAX_VALID_DURATION = Duration.ofMinutes(2);

    private final io.grpc.MethodDescriptor<com.hedera.hashgraph.proto.Transaction, com.hedera.hashgraph.proto.TransactionResponse> methodDescriptor;
    final com.hedera.hashgraph.proto.Transaction.Builder inner;
    final com.hedera.hashgraph.proto.AccountID nodeAccountId;
    final com.hedera.hashgraph.proto.TransactionID txnIdProto;

    // fully qualified to disambiguate
    private final java.time.Duration validDuration;

    private static final int PREFIX_LEN = 6;

    public final TransactionId id;

    Transaction(
        com.hedera.hashgraph.proto.Transaction.Builder inner,
        TransactionBodyOrBuilder body,
        MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> methodDescriptor)
    {
        this.inner = inner;
        this.nodeAccountId = body.getNodeAccountID();
        this.txnIdProto = body.getTransactionID();
        this.methodDescriptor = methodDescriptor;
        validDuration = DurationHelper.durationTo(body.getTransactionValidDuration());
        id = new TransactionId(txnIdProto);
    }

    public static Transaction fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        com.hedera.hashgraph.proto.Transaction inner = com.hedera.hashgraph.proto.Transaction.parseFrom(bytes);
        TransactionBody body = TransactionBody.parseFrom(inner.getBodyBytes());

        return new Transaction(inner.toBuilder(), body, methodForTxnBody(body));
    }

    public Transaction sign(PrivateKey<? extends PublicKey> privateKey) {
        SignatureMap.Builder sigMap = inner.getSigMapBuilder();

        for (SignaturePair sigPair : sigMap.getSigPairList()) {
            ByteString pubKeyPrefix = sigPair.getPubKeyPrefix();

            if (privateKey.publicKey.hasPrefix(pubKeyPrefix)) {
                throw new IllegalArgumentException(
                    "transaction already signed with key: " + privateKey.toString());
            }
        }

        sigMap.addSigPair(privateKey.sign(inner.getBodyBytes().toByteArray()));

        return this;
    }

    /**
     * @deprecated {use {@link TransactionId#getReceipt}}
     */
    @Deprecated
    public TransactionReceipt getReceipt(Client client) throws HederaStatusException {
        return id.getReceipt(client);
    }

    /**
     * @deprecated {use {@link TransactionId#getReceipt}}
     */
    @Deprecated
    public TransactionReceipt getReceipt(Client client, Duration timeout) throws HederaStatusException {
        return id.getReceipt(client, timeout);
    }

    /**
     * @deprecated {use {@link TransactionId#getReceiptAsync}}
     */
    @Deprecated
    public void getReceiptAsync(Client client, Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        id.getReceiptAsync(client, onReceipt, onError);
    }

    /**
     * @deprecated {use {@link TransactionId#getReceiptAsync}}
     */
    @Deprecated
    public void getReceiptAsync(Client client, Duration timeout, Consumer<TransactionReceipt> onReceipt, Consumer<HederaThrowable> onError) {
        id.getReceiptAsync(client, timeout, onReceipt, onError);
    }

    /**
     * @deprecated {use {@link TransactionId#getRecord}}
     */
    @Deprecated
    public TransactionRecord getRecord(Client client) throws HederaStatusException, HederaNetworkException {
        return id.getRecord(client);
    }

    /**
     * @deprecated {use {@link TransactionId#getRecord}}
     */
    @Deprecated
    public TransactionRecord getRecord(Client client, Duration timeout) throws HederaStatusException {
        return id.getRecord(client, timeout);
    }

    /**
     * @deprecated {use {@link TransactionId#getRecordAsync}}
     */
    @Deprecated
    public void getRecordAsync(Client client, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        id.getRecordAsync(client, onRecord, onError);
    }

    /**
     * @deprecated {use {@link TransactionId#getRecordAsync}}
     */
    @Deprecated
    public void getRecordAsync(Client client, Duration timeout, Consumer<TransactionRecord> onRecord, Consumer<HederaThrowable> onError) {
        id.getRecordAsync(client, timeout, onRecord, onError);
    }

    @Override
    public com.hedera.hashgraph.proto.Transaction toProto() {
        localValidate();
        return inner.build();
    }

    @Internal
    public com.hedera.hashgraph.proto.Transaction toProto(boolean requireSignature) {
        validate(requireSignature);
        return inner.build();
    }

    @Override
    protected MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    @Override
    protected Channel getChannel(Client client) {
        Node channel = client.getNodeForId(new AccountId(nodeAccountId));
        Objects.requireNonNull(channel, "Transaction.nodeAccountId not found on Client");

        return channel.getChannel();
    }

    @Override
    protected final void localValidate() {
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
    protected TransactionId mapResponse(TransactionResponse response) throws HederaStatusException {
        HederaStatusException.throwIfExceptional(response.getNodeTransactionPrecheckCode());
        return new TransactionId(txnIdProto);
    }

    @Override
    protected Duration getDefaultTimeout() {
        return validDuration;
    }

    public byte[] toBytes() {
        return toProto().toByteArray();
    }

    @Deprecated
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

            case DATA_NOT_SET:
                throw new IllegalArgumentException("method not set");

            default:
                throw new IllegalArgumentException("unsupported method");
        }
    }
}
