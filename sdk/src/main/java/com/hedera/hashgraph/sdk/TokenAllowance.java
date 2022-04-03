package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.GrantedTokenAllowance;
import com.hedera.hashgraph.sdk.proto.TokenRemoveAllowance;

import javax.annotation.Nullable;
import java.util.Objects;

public class TokenAllowance {
    @Nullable
    public final TokenId tokenId;
    @Nullable
    public final AccountId ownerAccountId;
    @Nullable
    public final AccountId spenderAccountId;
    public final long amount;

    TokenAllowance(
        @Nullable TokenId tokenId,
        @Nullable AccountId ownerAccountId,
        @Nullable AccountId spenderAccountId,
        long amount
    ) {
        this.tokenId = tokenId;
        this.ownerAccountId = ownerAccountId;
        this.spenderAccountId = spenderAccountId;
        this.amount = amount;
    }

    static TokenAllowance fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenAllowance allowanceProto) {
        return new TokenAllowance(
            allowanceProto.hasTokenId() ? TokenId.fromProtobuf(allowanceProto.getTokenId()) : null,
            allowanceProto.hasOwner() ? AccountId.fromProtobuf(allowanceProto.getOwner()) : null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            allowanceProto.getAmount()
        );
    }

    static TokenAllowance fromProtobuf(GrantedTokenAllowance allowanceProto) {
        return new TokenAllowance(
            allowanceProto.hasTokenId() ? TokenId.fromProtobuf(allowanceProto.getTokenId()) : null,
            null,
            allowanceProto.hasSpender() ? AccountId.fromProtobuf(allowanceProto.getSpender()) : null,
            allowanceProto.getAmount()
        );
    }

    static TokenAllowance fromProtobuf(TokenRemoveAllowance allowanceProto) {
        return new TokenAllowance(
            allowanceProto.hasTokenId() ? TokenId.fromProtobuf(allowanceProto.getTokenId()) : null,
            allowanceProto.hasOwner() ? AccountId.fromProtobuf(allowanceProto.getOwner()) : null,
            null,
            0
        );
    }

    public static TokenAllowance fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenAllowance.parseFrom(Objects.requireNonNull(bytes)));
    }

    TokenAllowance withOwner(@Nullable AccountId newOwnerAccountId) {
        return ownerAccountId != null ? this : new TokenAllowance(tokenId, newOwnerAccountId, spenderAccountId, amount);
    }

    void validateChecksums(Client client) throws BadEntityIdException {
        if (tokenId != null) {
            tokenId.validateChecksum(client);
        }
        if (ownerAccountId != null) {
            ownerAccountId.validateChecksum(client);
        }
        if (spenderAccountId != null) {
            spenderAccountId.validateChecksum(client);
        }
    }

    com.hedera.hashgraph.sdk.proto.TokenAllowance toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.TokenAllowance.newBuilder()
            .setAmount(amount);
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (ownerAccountId != null) {
            builder.setOwner(ownerAccountId.toProtobuf());
        }
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        return builder.build();
    }

    GrantedTokenAllowance toGrantedProtobuf() {
        var builder = GrantedTokenAllowance.newBuilder()
            .setAmount(amount);
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (spenderAccountId != null) {
            builder.setSpender(spenderAccountId.toProtobuf());
        }
        return builder.build();
    }

    TokenRemoveAllowance toWipeProtobuf() {
        var builder = TokenRemoveAllowance.newBuilder();
        if (tokenId != null) {
            builder.setTokenId(tokenId.toProtobuf());
        }
        if (ownerAccountId != null) {
            builder.setOwner(ownerAccountId.toProtobuf());
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
            .add("ownerAccountId", ownerAccountId)
            .add("spenderAccountId", spenderAccountId)
            .add("amount", amount)
            .toString();
    }
}
