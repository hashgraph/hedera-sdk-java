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
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoAllowance;
import com.hedera.hashgraph.sdk.proto.GrantedCryptoAllowance;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * An approved allowance of hbar transfers for a spender.
 *
 * See <a href="https://docs.hedera.com/guides/docs/hedera-api/basic-types/cryptoallowance”>Hedera Documentation</a>
 */
public class HbarAllowance {
    @Nullable
    public final AccountId ownerAccountId;
    @Nullable
    public final AccountId spenderAccountId;
    @Nullable
    public final Hbar amount;

    /**
     * Constructor.
     * @param ownerAccountId            the owner granting the allowance
     * @param spenderAccountId          the spender
     * @param amount                    the amount of hbar
     */
    HbarAllowance(@Nullable AccountId ownerAccountId, @Nullable AccountId spenderAccountId, @Nullable Hbar amount) {
        this.ownerAccountId = ownerAccountId;
        this.spenderAccountId = spenderAccountId;
        this.amount = amount;
    }

    /**
     * Create a hbar allowance from a crypto allowance protobuf.
     *
     * @param allowanceProto            the crypto allowance protobuf
     * @return                          the new hbar allowance
     */
    static HbarAllowance fromProtobuf(CryptoAllowance allowanceProto) {
        return new HbarAllowance(
            allowanceProto.hasOwner() ? AccountId.fromProtobuf(allowanceProto.getOwner()) : null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            Hbar.fromTinybars(allowanceProto.getAmount())
        );
    }

    /**
     * Create a hbar allowance from a granted crypto allowance protobuf.
     *
     * @param allowanceProto            the granted crypto allowance protobuf
     * @return                          the new hbar allowance
     */
    static HbarAllowance fromProtobuf(GrantedCryptoAllowance allowanceProto) {
        return new HbarAllowance(
            null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            Hbar.fromTinybars(allowanceProto.getAmount())
        );
    }

    /**
     * Create a hbar allowance from a byte array.
     *
     * @param bytes                     the byte array
     * @return                          the new hbar allowance
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    public static HbarAllowance fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CryptoAllowance.parseFrom(Objects.requireNonNull(bytes)));
    }

    /**
     * Validate that the client is configured correctly.
     *
     * @param client                    the client to verify
     * @throws BadEntityIdException
     */
    void validateChecksums(Client client) throws BadEntityIdException {
        if (ownerAccountId != null) {
            ownerAccountId.validateChecksum(client);
        }
        if (spenderAccountId != null) {
            spenderAccountId.validateChecksum(client);
        }
    }

    /**
     * Convert a crypto allowance into a protobuf.
     *
     * @return                          the protobuf
     */
    CryptoAllowance toProtobuf() {
        var builder = CryptoAllowance.newBuilder()
            .setAmount(amount.toTinybars());
        if (ownerAccountId != null) {
            builder.setOwner(ownerAccountId.toProtobuf());
        }
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        return builder.build();
    }

    /**
     * Convert a crypto allowance into a granted crypto allowance protobuf.
     *
     * @return                          the granted crypto allowance
     */
    GrantedCryptoAllowance toGrantedProtobuf() {
        var builder = GrantedCryptoAllowance.newBuilder()
            .setAmount(amount.toTinybars());
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        return builder.build();
    }

    /**
     * @return                          a byte array representation
     */
    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("ownerAccountId", ownerAccountId)
            .add("spenderAccountId", spenderAccountId)
            .add("amount", amount)
            .toString();
    }
}
