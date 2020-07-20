package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoDeleteLiveHashTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;

/**
 * At consensus, deletes a livehash associated to the given account. The transaction must be signed
 * by either the key of the owning account, or at least one of the keys associated to the livehash.
 */
public final class LiveHashDeleteTransaction extends SingleTransactionBuilder<LiveHashDeleteTransaction> {
    private final CryptoDeleteLiveHashTransactionBody.Builder builder;

    public LiveHashDeleteTransaction() {
        builder = CryptoDeleteLiveHashTransactionBody.newBuilder();
    }

    /**
     * The account owning the livehash
     *
     * @return {@code this}
     * @param accountId The AccountId to be set
     */
    public LiveHashDeleteTransaction setAccountId(AccountId accountId) {
        builder.setAccountOfLiveHash(accountId.toProtobuf());
        return this;
    }

    /**
     * The SHA-384 livehash to delete from the account
     *
     * @return {@code this}
     * @param hash The array of bytes to be set as hash
     */
    public LiveHashDeleteTransaction setHash(byte[] hash) {
        builder.setLiveHashToDelete(ByteString.copyFrom(hash));
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDeleteLiveHash(builder);
    }
}
