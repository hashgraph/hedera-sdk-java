package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.BoolValue;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.GrantedNftAllowance;
import com.hedera.hashgraph.sdk.proto.NftAllowance;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class TokenNftAllowance {
    @Nullable
    public final TokenId tokenId;
    @Nullable
    public final AccountId ownerAccountId;
    @Nullable
    public final AccountId spenderAccountId;
    public final List<Long> serialNumbers;
    @Nullable
    public final Boolean allSerials;

    TokenNftAllowance(
        @Nullable TokenId tokenId,
        @Nullable AccountId ownerAccountId,
        @Nullable AccountId spenderAccountId,
        Collection<Long> serialNumbers,
        @Nullable Boolean allSerials
    ) {
        this.tokenId = tokenId;
        this.ownerAccountId = ownerAccountId;
        this.spenderAccountId = spenderAccountId;
        this.serialNumbers = new ArrayList<>(serialNumbers);
        this.allSerials = allSerials;
    }

    static TokenNftAllowance copyFrom(TokenNftAllowance allowance) {
        return new TokenNftAllowance(
            allowance.tokenId,
            allowance.ownerAccountId,
            allowance.spenderAccountId,
            allowance.serialNumbers,
            allowance.allSerials
        );
    }

    static TokenNftAllowance fromProtobuf(NftAllowance allowanceProto) {
        return new TokenNftAllowance(
            allowanceProto.hasTokenId() ? TokenId.fromProtobuf(allowanceProto.getTokenId()) : null,
            allowanceProto.hasOwner() ? AccountId.fromProtobuf(allowanceProto.getOwner()) : null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            allowanceProto.getSerialNumbersList(),
            allowanceProto.hasApprovedForAll() ? allowanceProto.getApprovedForAll().getValue() : null
        );
    }

    static TokenNftAllowance fromProtobuf(GrantedNftAllowance allowanceProto) {
        return new TokenNftAllowance(
            allowanceProto.hasTokenId() ? TokenId.fromProtobuf(allowanceProto.getTokenId()) : null,
            null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            allowanceProto.getSerialNumbersList(),
            allowanceProto.getApprovedForAll()
        );
    }

    public static TokenNftAllowance fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NftAllowance.parseFrom(Objects.requireNonNull(bytes)));
    }

    TokenNftAllowance withOwner(@Nullable AccountId newOwnerAccountId) {
        return ownerAccountId != null ?
            this : new TokenNftAllowance(tokenId, newOwnerAccountId, spenderAccountId, serialNumbers, allSerials);
    }

    NftAllowance toProtobuf() {
        var builder = NftAllowance.newBuilder();
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (ownerAccountId != null) {
            builder.setOwner(ownerAccountId.toProtobuf());
        }
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        builder.addAllSerialNumbers(serialNumbers);
        if (allSerials != null) {
            builder.setApprovedForAll(BoolValue.newBuilder().setValue(allSerials).build());
        }
        return builder.build();
    }

    GrantedNftAllowance toGrantedProtobuf() {
        var builder = GrantedNftAllowance.newBuilder();
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        builder.addAllSerialNumbers(serialNumbers);
        if (allSerials != null) {
            builder.setApprovedForAll(allSerials);
        }
        return builder.build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }

    @Override
    public String toString() {
        var stringHelper = MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("ownerAccountId", ownerAccountId)
            .add("spenderAccountId", spenderAccountId);
        if (allSerials != null) {
            stringHelper.add("allSerials", allSerials);
        } else {
            stringHelper.add("serials", serialNumbers);
        }
        return stringHelper.toString();
    }
}
