package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;

public class LiveHash {
    public final AccountId accountId;
    public final ByteString hash;
    public final KeyList keys;
    public final Duration duration;

    private LiveHash(AccountId accountId, ByteString hash, KeyList keys, Duration duration) {
        this.accountId = accountId;
        this.hash = hash;
        this.keys = keys;
        this.duration = duration;
    }

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

    public ByteString toBytes() {
        return toProtobuf().toByteString();
    }

    protected static LiveHash fromProtobuf(com.hedera.hashgraph.sdk.proto.LiveHash liveHash) {
        return LiveHash.fromProtobuf(liveHash, null);
    }

    protected static LiveHash fromProtobuf(com.hedera.hashgraph.sdk.proto.LiveHash liveHash, @Nullable NetworkName networkName) {
        return new LiveHash(
            AccountId.fromProtobuf(liveHash.getAccountId(), networkName),
            liveHash.getHash(),
            KeyList.fromProtobuf(liveHash.getKeys(), null, networkName),
            DurationConverter.fromProtobuf(liveHash.getDuration())
        );
    }

    public static LiveHash fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.LiveHash.parseFrom(bytes).toBuilder().build());
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
