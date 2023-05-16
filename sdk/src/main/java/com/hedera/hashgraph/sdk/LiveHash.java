/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import java.time.Duration;

/**
 *A hash (presumably of some kind of credential or certificate), along with a
 * list of keys (each of which is either a primitive or a threshold key). Each
 * of them must reach its threshold when signing the transaction, to attach
 * this livehash to this account. At least one of them must reach its
 * threshold to delete this livehash from this account.
 *
 * See <a href="https://docs.hedera.com/guides/core-concepts/accounts#livehash">Hedera Documentation</a>
 */
public class LiveHash {

    /**
     * The account to which the livehash is attached
     */
    public final AccountId accountId;

    /**
     * The SHA-384 hash of a credential or certificate
     */
    public final ByteString hash;

    /**
     * A list of keys (primitive or threshold), all of which must sign to attach the livehash to an account, and any one of which can later delete it.
     */
    public final KeyList keys;

    /**
     * The duration for which the livehash will remain valid
     */
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
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
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
     * Extract the byte array.
     *
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
