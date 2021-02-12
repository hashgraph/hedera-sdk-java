package com.hedera.hashgraph.sdk.schedule;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import io.grpc.MethodDescriptor;

public class ScheduleSignTransaction extends SingleTransactionBuilder<ScheduleSignTransaction> {
    private final ScheduleSignTransactionBody.Builder builder = bodyBuilder.getScheduleSignBuilder();

    public ScheduleSignTransaction() {
        super();
    }

    public ScheduleSignTransaction setScheduleId(ScheduleId scheduleId) {
        builder.setScheduleID(scheduleId.toProto());
        return this;
    }

    public ScheduleSignTransaction addSignature(PublicKey publicKey, byte[] signature) {
        SignatureMap.Builder sigMap = builder.getSigMapBuilder();

        SignaturePair.Builder sigPairBuilder = SignaturePair.newBuilder()
            .setPubKeyPrefix(ByteString.copyFrom(publicKey.toBytes()));

        switch (publicKey.getSignatureCase()) {
            case CONTRACT:
                throw new UnsupportedOperationException("contract signatures are not currently supported");
            case ED25519:
                sigPairBuilder.setEd25519(ByteString.copyFrom(signature));
                break;
            case RSA_3072:
                sigPairBuilder.setRSA3072(ByteString.copyFrom(signature));
                break;
            case ECDSA_384:
                sigPairBuilder.setECDSA384(ByteString.copyFrom(signature));
                break;
            case SIGNATURE_NOT_SET:
                throw new IllegalStateException("PublicKey.getSignatureCase() returned SIGNATURE_NOT_SET");
        }

        sigMap.addSigPair(sigPairBuilder);

        return this;
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ScheduleServiceGrpc.getSignScheduleMethod();
    }

    @Override
    protected void doValidate() {
        // Do nothing
    }
}
