package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.TransactionSigner;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import org.bouncycastle.util.encoders.Hex;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Transaction extends HederaCall<com.hedera.hashgraph.proto.Transaction, TransactionResponse, TransactionId, Transaction> {
    static final Duration MAX_VALID_DURATION = Duration.ofMinutes(2);

    private final io.grpc.MethodDescriptor<com.hedera.hashgraph.proto.Transaction, com.hedera.hashgraph.proto.TransactionResponse> methodDescriptor;
    final List<RealTransaction> txns;
    final com.hedera.hashgraph.proto.TransactionID txnIdProto;

    // fully qualified to disambiguate
    private final java.time.Duration validDuration;

    private static final int PREFIX_LEN = 6;

    public final TransactionId id;

    Transaction(
        List<com.hedera.hashgraph.proto.TransactionBody> txns,
        MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> methodDescriptor)
    {
        if (txns.isEmpty()) {
            throw new IllegalArgumentException("transactions list must not be empty");
        }

        this.txns = txns.stream().map(RealTransaction::new).collect(Collectors.toList());
        this.txnIdProto = txns.get(0).getTransactionID();
        this.methodDescriptor = methodDescriptor;
        validDuration = DurationHelper.durationTo(txns.get(0).getTransactionValidDuration());
        id = new TransactionId(txnIdProto);
    }

    private Transaction(com.hedera.hashgraph.proto.Transaction inner) {
        RealTransaction txn = new RealTransaction(inner.toBuilder());
        this.txns = Collections.singletonList(txn);
        methodDescriptor = methodForTxnBody(txn.body);
        txnIdProto = txn.body.getTransactionID();
        validDuration = DurationHelper.durationTo(txn.body.getTransactionValidDuration());
        id = new TransactionId(txnIdProto);
    }

    public static Transaction fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return new Transaction(com.hedera.hashgraph.proto.Transaction.parseFrom(bytes));
    }

    public Transaction sign(PrivateKey<? extends PublicKey> privateKey) {
        return signWith(privateKey.publicKey, privateKey::sign);
    }

    /**
     * Sign the transaction with a callback that may block waiting for user confirmation.
     *
     * @param publicKey the public key that pairs with the signature.
     *                  Currently only {@link Ed25519PublicKey} is allowed.
     * @param signer the labmda to generate the signature.
     * @return {@code this} for fluent usage.
     * @see TransactionSigner
     */
    public Transaction signWith(PublicKey publicKey, TransactionSigner signer) {
        for (RealTransaction txn : txns) {
            SignatureMap.Builder sigMap = txn.transaction.getSigMapBuilder();

            for (SignaturePair sigPair : sigMap.getSigPairList()) {
                ByteString pubKeyPrefix = sigPair.getPubKeyPrefix();

                if (publicKey.hasPrefix(pubKeyPrefix)) {
                    throw new IllegalArgumentException(
                        "transaction already signed with key: " + publicKey.toString());
                }
            }

            ByteString signatureBytes = ByteString.copyFrom(
                signer.signTransaction(txn.body.toByteArray()));

            SignaturePair.Builder sigPairBuilder = SignaturePair.newBuilder()
                .setPubKeyPrefix(ByteString.copyFrom(publicKey.toBytes()));

            switch (publicKey.getSignatureCase()) {
                case CONTRACT:
                    throw new UnsupportedOperationException("contract signatures are not currently supported");
                case ED25519:
                    sigPairBuilder.setEd25519(signatureBytes);
                    break;
                case RSA_3072:
                    sigPairBuilder.setRSA3072(signatureBytes);
                    break;
                case ECDSA_384:
                    sigPairBuilder.setECDSA384(signatureBytes);
                    break;
                case SIGNATURE_NOT_SET:
                    throw new IllegalStateException("PublicKey.getSignatureCase() returned SIGNATURE_NOT_SET");
            }

            sigMap.addSigPair(sigPairBuilder);
        }

        return this;
    }

    @Override
    public final TransactionId execute(Client client, Duration timeout) throws HederaStatusException, HederaNetworkException, LocalValidationException {
        // Sign with the operator if there is a client; the client has an operator; and, the transaction
        // has a transaction ID that matches that operator ( which it would unless overridden ).
        if (client.getOperatorPublicKey() != null && client.getOperatorSigner() != null
            && client.getOperatorId() != null
            && client.getOperatorId().equals(new AccountId(txnIdProto.getAccountID())))
        {
            signWith(client.getOperatorPublicKey(), client.getOperatorSigner());
        }

        // if the user didn't specify a node address, we generate and sign a transaction for every node
        // and then try them in order
        // this improves user experience in the case individual nodes are temporarily unreachable
        Exception lastException = null;
        Instant callExpires = Instant.now().plus(timeout);

        for (RealTransaction txn : txns) {
            if (callExpires.isBefore(Instant.now())) {
                break;
            }

            try {
                txn.execute(client, timeout);
                return id;
            } catch (HederaStatusException e) {
                if (e.status == Status.Busy) {
                    lastException = e;
                } else {
                    throw e;
                }
            } catch (Exception e) {
                lastException = e;
            }
        }

        if (lastException instanceof HederaNetworkException) {
            throw (HederaNetworkException) lastException;
        } else if (lastException instanceof HederaStatusException) {
            throw (HederaStatusException) lastException;
        } else {
            throw new RuntimeException("BUG loop did not save an exception to throw");
        }
    }

    @Override
    public void executeAsync(Client client, Duration retryTimeout, Consumer<TransactionId> onSuccess, Consumer<HederaThrowable> onError) {
        // Sign with the operator if there is a client; the client has an operator; and, the transaction
        // has a transaction ID that matches that operator ( which it would unless overridden ).
        if (client.getOperatorPublicKey() != null && client.getOperatorSigner() != null
            && client.getOperatorId() != null
            && client.getOperatorId().equals(new AccountId(txnIdProto.getAccountID())))
        {
            signWith(client.getOperatorPublicKey(), client.getOperatorSigner());
        }

        super.executeAsync(client, retryTimeout, onSuccess, onError);
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
        return txns.get(0).toProto();
    }

    @Internal
    public com.hedera.hashgraph.proto.Transaction toProto(boolean requireSignature) {
        return txns.get(0).toProto();
    }

    @Override
    protected MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> getMethod() {
        return methodDescriptor;
    }

    @Override
    protected Channel getChannel(Client client) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final void localValidate() {
        SignatureMapOrBuilder sigMap = txns.get(0).transaction.getSigMapOrBuilder();

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
    protected AccountId getNodeId() {
        throw new UnsupportedOperationException("execution should delegate to RealTransaction");
    }

    @Override
    protected TransactionId mapResponse(TransactionResponse response) throws HederaStatusException {
        throw new UnsupportedOperationException("execution should delegate to RealTransaction");
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

    private final class RealTransaction extends HederaCall<com.hedera.hashgraph.proto.Transaction, TransactionResponse, Void, RealTransaction> {
        private final com.hedera.hashgraph.proto.Transaction.Builder transaction;
        private final TransactionBody body;
        private final AccountId nodeAccountId;

        @Override
        protected AccountId getNodeId() {
            return nodeAccountId;
        }

        private RealTransaction(com.hedera.hashgraph.proto.Transaction.Builder transaction) {
            this.transaction = transaction;
            try {
                this.body = TransactionBody.parseFrom(transaction.getBodyBytes());
            } catch (InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
            nodeAccountId = new AccountId(body.getNodeAccountID());
        }

        private RealTransaction(TransactionBody body) {
            transaction = com.hedera.hashgraph.proto.Transaction.newBuilder().setBodyBytes(body.toByteString());
            this.body = body;
            nodeAccountId = new AccountId(body.getNodeAccountID());
        }

        @Override
        protected MethodDescriptor<com.hedera.hashgraph.proto.Transaction, TransactionResponse> getMethod() {
            return methodForTxnBody(body);
        }

        @Override
        public com.hedera.hashgraph.proto.Transaction toProto() {
            return transaction.build();
        }

        @Override
        protected Channel getChannel(Client client) {
            return client.getNodeById(nodeAccountId).getChannel();
        }

        @Override
        protected Void mapResponse(TransactionResponse raw) throws HederaStatusException {
            HederaPrecheckStatusException.throwIfExceptional(nodeAccountId, raw.getNodeTransactionPrecheckCode(), id);
            return null;
        }

        @Override
        protected void localValidate() throws LocalValidationException {

        }
    }
}
