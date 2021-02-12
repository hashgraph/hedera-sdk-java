package com.hedera.hashgraph.sdk.schedule;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.proto.*;
import com.hedera.hashgraph.sdk.SingleTransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import io.grpc.MethodDescriptor;

public class ScheduleCreateTransaction extends SingleTransactionBuilder<ScheduleCreateTransaction> {
    private final ScheduleCreateTransactionBody.Builder builder = bodyBuilder.getScheduleCreateBuilder();

    public ScheduleCreateTransaction() {
        super();
    }

    public ScheduleCreateTransaction setTransaction(com.hedera.hashgraph.sdk.Transaction transaction) {
        ScheduleCreateTransaction other = transaction.schedule();
        this.builder.setTransactionBody(other.builder.getTransactionBody());
        this.builder.getSigMapBuilder().mergeFrom(other.builder.getSigMap());
        return this;
    }

    public ScheduleCreateTransaction setAdminKey(PublicKey publicKey) {
        builder.setAdminKey(publicKey.toKeyProto());
        return this;
    }

    public ScheduleCreateTransaction setMemo(String memo) {
        builder.setMemo(memo);
        return this;
    }

    public ScheduleCreateTransaction setPayerAccountId(AccountId payerAccountId) {
        builder.setPayerAccountID(payerAccountId.toProto());
        return this;
    }

    public ScheduleCreateTransaction addSignature(PublicKey publicKey, byte[] signature) {
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
        return ScheduleServiceGrpc.getCreateScheduleMethod();
    }

    @Override
    protected void doValidate() {
        // Do nothing
    }
}
