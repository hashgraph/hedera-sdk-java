package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.BoolValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.NftAllowance;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class TokenNftAllowance {
    @Nullable
    public final TokenId tokenId;
    @Nullable
    public final AccountId spenderAccountId;
    @Nullable
    public final List<Long> serialNumbers;

    TokenNftAllowance(
        @Nullable TokenId tokenId,
        @Nullable AccountId spenderAccountId,
        @Nullable Collection<Long> serialNumbers
    ) {
        this.tokenId = tokenId;
        this.spenderAccountId = spenderAccountId;
        this.serialNumbers = serialNumbers != null ? new ArrayList<>(serialNumbers) : null;
    }

    static TokenNftAllowance copyFrom(TokenNftAllowance allowance) {
        return new TokenNftAllowance(allowance.tokenId, allowance.spenderAccountId, allowance.serialNumbers);
    }

    static TokenNftAllowance fromProtobuf(NftAllowance allowanceProto) {
        return new TokenNftAllowance(
            allowanceProto.hasTokenId() ? TokenId.fromProtobuf(allowanceProto.getTokenId()) : null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            (allowanceProto.hasApprovedForAll() && allowanceProto.getApprovedForAll().getValue()) ?
                null : allowanceProto.getSerialNumbersList()
        );
    }

    public static TokenNftAllowance fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NftAllowance.parseFrom(Objects.requireNonNull(bytes)));
    }

    NftAllowance toProtobuf() {
        var builder = NftAllowance.newBuilder();
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        if (serialNumbers != null) {
            builder.addAllSerialNumbers(serialNumbers);
        } else {
            builder.setApprovedForAll(BoolValue.newBuilder().setValue(true).build());
        }
        return builder.build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("spenderAccountId", spenderAccountId)
            .add("serials", serialNumbers)
            .toString();
    }
}
