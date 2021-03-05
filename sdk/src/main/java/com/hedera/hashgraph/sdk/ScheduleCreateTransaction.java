package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.*;

public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    private final ScheduleCreateTransactionBody.Builder builder;
    private final SignatureMap.Builder signatureBuilder;

    public ScheduleCreateTransaction() {
        builder = ScheduleCreateTransactionBody.newBuilder();
        signatureBuilder = builder.getSigMap().toBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleCreateTransaction(
        List<AccountId> nodeAccountIds,
        ByteString bodyBytes,
        SignatureMap signatureMap
    ) {
        this();

        this.nodeAccountIds = nodeAccountIds;
        this.builder.setTransactionBody(bodyBytes);
        this.signatureBuilder.mergeFrom(signatureMap);
    }

    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleCreate().toBuilder();
        signatureBuilder = builder.getSigMap().toBuilder();
    }

    public Map<PublicKey, byte[]> getScheduleSignatures() {
        var map = new HashMap<PublicKey, byte[]>();

        for (var sigPair : signatureBuilder.getSigPairList()) {
            map.put(
                PublicKey.fromBytes(sigPair.getPubKeyPrefix().toByteArray()),
                sigPair.getEd25519().toByteArray()
            );
        }

        return map;
    }

    public ScheduleCreateTransaction addScheduleSignature(PublicKey publicKey, byte[] signature) {
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

    public ScheduleCreateTransaction setTransaction(Transaction<?> transaction) {
        requireNotFrozen();
        builder.setTransactionBody(transaction.signedTransactions.get(0).getBodyBytes());
        signatureBuilder.mergeFrom(transaction.signatures.get(0).build());
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
