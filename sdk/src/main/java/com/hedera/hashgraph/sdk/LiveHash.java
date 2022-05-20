package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.threeten.bp.Duration;

/**
 *A hash (presumably of some kind of credential or certificate), along with a
 * list of keys (each of which is either a primitive or a threshold key). Each
 * of them must reach its threshold when signing the transaction, to attach
 * this livehash to this account. At least one of them must reach its
 * threshold to delete this livehash from this account.
 *
 * {@link https://docs.hedera.com/guides/core-concepts/accounts#livehash}
 */
public class LiveHash {
    public final AccountId accountId;
    public final ByteString hash;
    public final KeyList keys;
    public final Duration duration;

    /**
     * Constructor.
     *
     * @param accountId                 the account id
     * @param hash                      the hash
     * @param keys                      the key list
     * @param duration                  the duration
     */
    private LiveHash(AccountId accountId, ByteString hash, KeyList keys, Duration duration) {
        this.accountId = accountId;
        this.hash = hash;
        this.keys = keys;
        this.duration = duration;
    }

    /**
     * Create a live hash from a protobuf.
     *
     * @param liveHash                  the protobuf
     * @return                          the new live hash
     */
    protected static LiveHash fromProtobuf(com.hedera.hashgraph.sdk.proto.LiveHash liveHash) {
        return new LiveHash(
            AccountId.fromProtobuf(liveHash.getAccountId()),
            liveHash.getHash(),
            KeyList.fromProtobuf(liveHash.getKeys(), null),
            DurationConverter.fromProtobuf(liveHash.getDuration())
        );
    }

    /**
     * Create a live hash from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new live hash
     * @throws InvalidProtocolBufferException
     */
    public static LiveHash fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.LiveHash.parseFrom(bytes).toBuilder().build());
    }

    /**
     * Convert the live hash into a protobuf.
     *
     * @return                          the protobuf
     */
    protected com.hedera.hashgraph.sdk.proto.LiveHash toProtobuf() {
        var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();
        for (Key key : keys) {
            keyList.addKeys(key.toProtobufKey());
        }

        return com.hedera.hashgraph.sdk.proto.LiveHash.newBuilder()
            .setAccountId(accountId.toProtobuf())
            .setHash(hash)
            .setKeys(keyList)
            .setDuration(DurationConverter.toProtobuf(duration))
            .build();
    }

    /**
     * @return                          the byte array representation
     */
    public ByteString toBytes() {
        return toProtobuf().toByteString();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("accountId", accountId)
            .add("hash", hash.toByteArray())
            .add("keys", keys)
            .add("duration", duration)
            .toString();
    }
}
