package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteLiveHashTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;

/**
 * At consensus, deletes a livehash associated to the given account. The transaction must be signed
 * by either the key of the owning account, or at least one of the keys associated to the livehash.
 */
public final class LiveHashDeleteTransaction extends Transaction<LiveHashDeleteTransaction> {
    private final CryptoDeleteLiveHashTransactionBody.Builder builder;

    public LiveHashDeleteTransaction() {
        builder = CryptoDeleteLiveHashTransactionBody.newBuilder();
    }

    LiveHashDeleteTransaction(TransactionBody body) {
        super(body);

        builder = body.getCryptoDeleteLiveHash().toBuilder();
    }

    @Nullable
    public AccountId getAccountId() {
        return builder.hasAccountOfLiveHash() ? AccountId.fromProtobuf(builder.getAccountOfLiveHash()) : null;
    }

    /**
     * The account owning the livehash
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public LiveHashDeleteTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAccountOfLiveHash(accountId.toProtobuf());
        return this;
    }

    public ByteString getHash() {
        return builder.getLiveHashToDelete();
    }

    /**
     * The SHA-384 livehash to delete from the account
     *
     * @param hash The array of bytes to be set as hash
     * @return {@code this}
     */
    public LiveHashDeleteTransaction setHash(byte[] hash) {
        return setHash(ByteString.copyFrom(hash));
    }

    /**
     * The SHA-384 livehash to delete from the account
     *
     * @param hash The array of bytes to be set as hash
     * @return {@code this}
     */
    public LiveHashDeleteTransaction setHash(ByteString hash) {
        requireNotFrozen();
        builder.setLiveHashToDelete(hash);
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getDeleteLiveHashMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDeleteLiveHash(builder);
        return true;
    }
}
