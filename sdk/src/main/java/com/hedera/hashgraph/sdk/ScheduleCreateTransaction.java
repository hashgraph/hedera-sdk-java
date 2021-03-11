package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java8.util.function.Function;

import java.util.*;

public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    private final ScheduleCreateTransactionBody.Builder builder;
    private final SignatureMap.Builder signatureBuilder;

    public ScheduleCreateTransaction() {
        builder = ScheduleCreateTransactionBody.newBuilder();
        signatureBuilder = builder.getSigMap().toBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleCreate().toBuilder();
        signatureBuilder = builder.getSigMap().toBuilder();
    }

    ScheduleCreateTransaction setTransactionBodyBytes(ByteString bodyBytes) {
        requireNotFrozen();
        builder.setTransactionBody(bodyBytes);
        return this;
    }

    public Map<PublicKey, byte[]> getScheduledSignatures() {
        var map = new HashMap<PublicKey, byte[]>();

        for (var sigPair : signatureBuilder.getSigPairList()) {
            map.put(
                PublicKey.fromBytes(sigPair.getPubKeyPrefix().toByteArray()),
                sigPair.getEd25519().toByteArray()
            );
        }

        return map;
    }

    public ScheduleCreateTransaction addScheduledSignature(PublicKey publicKey, byte[] signature) {
        signatureBuilder.addSigPair(publicKey.toSignaturePairProtobuf(signature));

        return this;
    }

    public AccountId getPayerAccountId() {
        return AccountId.fromProtobuf(builder.getPayerAccountID());
    }

    public ScheduleCreateTransaction setPayerAccountId(AccountId id) {
        requireNotFrozen();
        builder.setPayerAccountID(id.toProtobuf());
        return this;
    }


    public Key getAdminKey() {
        return Key.fromProtobufKey(builder.getAdminKey());
    }

    public ScheduleCreateTransaction setAdminKey(Key key) {
        requireNotFrozen();
        builder.setAdminKey(key.toProtobufKey());
        return this;
    }

    public ScheduleCreateTransaction setScheduledTransaction(Transaction<?> transaction) {
        requireNotFrozen();
        transaction.requireNotFrozen();

        var scheduled = transaction.schedule();

        builder.setTransactionBody(scheduled.builder.getTransactionBody());
        signatureBuilder.clearSigPair();

        if (!scheduled.transactionIds.isEmpty() && transactionIds.isEmpty()) {
            transactionIds = scheduled.transactionIds;
        } else if (!scheduled.transactionIds.isEmpty() && !transactionIds.isEmpty()) {
            var scheduledTransactionId = scheduled.transactionIds.get(0);

            // Set `scheduled` to `true` to make it easier to compare
            var thisTransactionId = transactionIds.get(0).setScheduled(true);

            if (!thisTransactionId.equals(scheduledTransactionId)) {
                throw new IllegalStateException(
                    "Transaction being scheduled has a transaction ID already set, but the current " +
                    "`ScheduleCreateTransaction` already has a transaction ID set which differs from the " +
                    "transaction being scheduled."
                );
            }

            // Revert `scheduled` to `false`
            transactionIds.get(0).setScheduled(false);
        }

        return this;
    }

    public String getScheduleMemo() {
        return builder.getMemo();
    }

    public ScheduleCreateTransaction setScheduleMemo(String memo) {
        requireNotFrozen();
        builder.setMemo(memo);
        return this;
    }

    public ScheduleCreateTransaction signScheduled(PrivateKey key) {
        return signScheduledWith(key.getPublicKey(), key::sign);
    }

    public ScheduleCreateTransaction signScheduledWithOperator(Client client) {
        var operator = client.getOperator();

        if (operator == null) {
            throw new IllegalStateException(
                "`client` must have an `operator` to sign with the operator");
        }

        return signScheduledWith(operator.publicKey, operator.transactionSigner);
    }

    public ScheduleCreateTransaction signScheduledWith(PublicKey publicKey, Function<byte[], byte[]> transactionSigner) {
        requireNotFrozen();

        if (keyAlreadySignedScheduled(publicKey)) {
            return this;
        }

        var signature = transactionSigner.apply(builder.getTransactionBody().toByteArray());
        signatureBuilder.addSigPair(publicKey.toSignaturePairProtobuf(signature));

        return this;
    }

    protected boolean keyAlreadySignedScheduled(PublicKey key) {
        for (var sigPair : signatureBuilder.getSigPairList()) {
            if (ByteString.copyFrom(key.toBytes()).startsWith(sigPair.getPubKeyPrefix())) {
                return true;
            }
        }

        return false;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getCreateScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleCreate(builder.setSigMap(signatureBuilder));
        return true;
    }

    @Override
    final com.hedera.hashgraph.sdk.TransactionResponse mapResponse(
        com.hedera.hashgraph.sdk.proto.TransactionResponse transactionResponse,
        AccountId nodeId,
        com.hedera.hashgraph.sdk.proto.Transaction request
    ) {
        var transactionId = Objects.requireNonNull(getTransactionId()).setScheduled(true);
        var hash = hash(request.getSignedTransactionBytes().toByteArray());
        nextTransactionIndex = (nextTransactionIndex + 1) % transactionIds.size();
        return new com.hedera.hashgraph.sdk.TransactionResponse(nodeId, transactionId, hash, transactionId);
    }
}
