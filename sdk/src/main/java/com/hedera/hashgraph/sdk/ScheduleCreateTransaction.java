package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ScheduleCreateTransaction extends Transaction<ScheduleCreateTransaction> {
    private final ScheduleCreateTransactionBody.Builder builder;

    public ScheduleCreateTransaction() {
        builder = ScheduleCreateTransactionBody.newBuilder();

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
        this.builder.mergeSigMap(signatureMap);
    }

    ScheduleCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleCreate().toBuilder();
    }

    public Map<PublicKey, byte[]> getScheduleSignatures() {
        var map = new HashMap<PublicKey, byte[]>();

        for (var sigPair : builder.getSigMap().getSigPairList()) {
            map.put(
                PublicKey.fromBytes(sigPair.getPubKeyPrefix().toByteArray()),
                sigPair.getEd25519().toByteArray()
            );
        }

        return map;
    }

    public ScheduleCreateTransaction addScheduleSignature(PublicKey publicKey, byte[] signature) {
        SignatureMap.Builder sigMap = builder.getSigMap().toBuilder();
        sigMap.addSigPair(publicKey.toSignaturePairProtobuf(signature));
        builder.setSigMap(sigMap);

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
        this.builder.setTransactionBody(transaction.signedTransactions.get(0).getBodyBytes());
        this.builder.mergeSigMap(transaction.signatures.get(0).build());
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getCreateScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleCreate(builder);
        return true;
    }
}
