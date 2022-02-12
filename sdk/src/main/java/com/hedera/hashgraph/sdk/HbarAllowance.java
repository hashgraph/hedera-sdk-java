package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoAllowance;

import javax.annotation.Nullable;
import java.util.Objects;

public class HbarAllowance {
    @Nullable
    public final AccountId ownerAccountId;
    @Nullable
    public final AccountId spenderAccountId;
    public final Hbar amount;

    HbarAllowance(@Nullable AccountId ownerAccountId, @Nullable AccountId spenderAccountId, Hbar amount) {
        this.ownerAccountId = ownerAccountId;
        this.spenderAccountId = spenderAccountId;
        this.amount = amount;
    }

    static HbarAllowance fromProtobuf(CryptoAllowance allowanceProto) {
        return new HbarAllowance(
            allowanceProto.hasOwner() ? AccountId.fromProtobuf(allowanceProto.getOwner()) : null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            Hbar.fromTinybars(allowanceProto.getAmount())
        );
    }

    public static HbarAllowance fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(CryptoAllowance.parseFrom(Objects.requireNonNull(bytes)));
    }

    HbarAllowance withOwner(@Nullable AccountId newOwnerAccountId) {
        return new HbarAllowance(newOwnerAccountId, spenderAccountId, amount);
    }

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
