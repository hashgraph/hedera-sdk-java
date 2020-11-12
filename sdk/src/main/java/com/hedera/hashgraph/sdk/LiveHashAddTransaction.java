package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoAddLiveHashTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.LiveHash;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import java.time.Duration;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;

/**
 * A hash---presumably of some kind of credential or certificate---along with a list of keys,
 * each of which may be either a primitive or a threshold key.
 */
public final class LiveHashAddTransaction extends Transaction<LiveHashAddTransaction> {
    private final CryptoAddLiveHashTransactionBody.Builder builder;
    private final LiveHash.Builder hashBuilder;

    public LiveHashAddTransaction() {
        builder = CryptoAddLiveHashTransactionBody.newBuilder();
        hashBuilder = LiveHash.newBuilder();
    }

    LiveHashAddTransaction(HashMap<TransactionId, HashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) {
        super(txs.values().iterator().next());

        builder = bodyBuilder.getCryptoAddLiveHash().toBuilder();
        hashBuilder = builder.getLiveHash().toBuilder();
    }

    @Nullable
    public AccountId getAccountId() {
        return hashBuilder.hasAccountId() ? AccountId.fromProtobuf(hashBuilder.getAccountId()) : null;
    }

    /**
     * The account to which the livehash is attached
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public LiveHashAddTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        hashBuilder.setAccountId(accountId.toProtobuf());
        return this;
    }

    public ByteString getHash() {
        return hashBuilder.getHash();
    }

    /**
     * The SHA-384 hash of a credential or certificate.
     *
     * @param hash The array of bytes to be set as the hash
     * @return {@code this}
     */
    public LiveHashAddTransaction setHash(byte[] hash) {
        return setHash(ByteString.copyFrom(hash));
    }

    /**
     * The SHA-384 hash of a credential or certificate.
     *
     * @param hash The array of bytes to be set as the hash
     * @return {@code this}
     */
    public LiveHashAddTransaction setHash(ByteString hash) {
        requireNotFrozen();
        hashBuilder.setHash(hash);
        return this;
    }

    public Collection<Key> getKeys() {
        return KeyList.fromProtobuf(hashBuilder.getKeys(), null);
    }

    /**
     * A list of keys (primitive or threshold), all of which must sign to attach the livehash to an
     * account, and any one of which can later delete it.
     *
     * @param keys The Key or Keys to be set
     * @return {@code this}
     */
    public LiveHashAddTransaction setKeys(Key... keys) {
        requireNotFrozen();

        var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();

        for (Key key : keys) {
            keyList.addKeys(key.toKeyProtobuf());
        }

        hashBuilder.setKeys(keyList);

        return this;
    }

    @Nullable
    public Duration getDuration() {
        return hashBuilder.hasDuration() ? DurationConverter.fromProtobuf(hashBuilder.getDuration()) : null;
    }

    /**
     * The duration for which the livehash will remain valid
     *
     * @param duration The Duration to be set
     * @return {@code this}
     */
    public LiveHashAddTransaction setDuration(Duration duration) {
        requireNotFrozen();
        hashBuilder.setDuration(DurationConverter.toProtobuf(duration));
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getAddLiveHashMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoAddLiveHash(builder.setLiveHash(hashBuilder));
        return true;
    }
}
