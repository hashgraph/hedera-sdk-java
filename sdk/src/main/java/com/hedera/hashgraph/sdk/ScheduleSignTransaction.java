package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ScheduleSignTransactionBody;
import com.hedera.hashgraph.sdk.proto.ScheduleServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SignatureMap;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ScheduleSignTransaction extends Transaction<ScheduleSignTransaction> {
    private final ScheduleSignTransactionBody.Builder builder;

    public ScheduleSignTransaction() {
        builder = ScheduleSignTransactionBody.newBuilder();

        setMaxTransactionFee(new Hbar(5));
    }

    ScheduleSignTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getScheduleSign().toBuilder();
    }

    public ScheduleId getScheduleId() {
        return ScheduleId.fromProtobuf(builder.getScheduleID());
    }

    public ScheduleSignTransaction setScheduleId(ScheduleId id) {
        requireNotFrozen();
        builder.setScheduleID(id.toProtobuf());
        return this;
    }

    public ScheduleSignTransaction clearScheduleId() {
        requireNotFrozen();
        builder.clearScheduleID();
        return this;
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

    public ScheduleSignTransaction addScheduleSignature(PublicKey publicKey, byte[] signature) {
        SignatureMap.Builder sigMap = builder.getSigMap().toBuilder();
        sigMap.addSigPair(publicKey.toSignaturePairProtobuf(signature));
        builder.setSigMap(sigMap);

        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return ScheduleServiceGrpc.getSignScheduleMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setScheduleSign(builder);
        return true;
    }
}
