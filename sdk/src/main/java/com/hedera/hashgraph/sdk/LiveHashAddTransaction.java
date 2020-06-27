package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.CryptoAddLiveHashTransactionBody;
import com.hedera.hashgraph.sdk.proto.LiveHash;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import org.threeten.bp.Duration;

/**
 * A hash---presumably of some kind of credential or certificate---along with a list of keys,
 * each of which may be either a primitive or a threshold key.
 */
public final class LiveHashAddTransaction extends TransactionBuilder<LiveHashAddTransaction> {
    private final CryptoAddLiveHashTransactionBody.Builder builder;
    private final LiveHash.Builder liveHash;

    public LiveHashAddTransaction() {
        builder = CryptoAddLiveHashTransactionBody.newBuilder();
        liveHash = LiveHash.newBuilder();
        builder.setLiveHash(liveHash);
    }

    /**
     * The account to which the livehash is attached
     *
     * @return {@code this}
     */
    public LiveHashAddTransaction setAccountId(AccountId accountId) {
        liveHash.setAccountId(accountId.toProtobuf());
        return this;
    }

    /**
     * The SHA-384 hash of a credential or certificate
     *
     * @return {@code this}
     */
    public LiveHashAddTransaction setHash(byte[] hash) {
        liveHash.setHash(ByteString.copyFrom(hash));
        return this;
    }

    /**
     * A list of keys (primitive or threshold), all of which must sign to attach the livehash to an
     * account, and any one of which can later delete it.
     *
     * @return {@code this}
     */
    public LiveHashAddTransaction setKeys(Key... keys) {
        var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();
        for (Key key : keys) {
            keyList.addKeys(key.toKeyProtobuf());
        }
        liveHash.setKeys(keyList);
        return this;
    }

    /**
     * The duration for which the livehash will remain valid
     *
     * @return {@code this}
     */
    public LiveHashAddTransaction setDuration(Duration duration) {
        liveHash.setDuration(DurationConverter.toProtobuf(duration));
        return this;
    }

    @Override
    void onBuild(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoAddLiveHash(builder);
    }
}
