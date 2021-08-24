package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;

public class TokenAssociation {
    public final TokenId tokenId;
    public final AccountId accountId;

    private TokenAssociation(TokenId tokenId, AccountId accountId) {
        this.tokenId = tokenId;
        this.accountId = accountId;
    }

    static TokenAssociation fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenAssociation tokenAssociation) {
        return new TokenAssociation(
            tokenAssociation.hasTokenId() ? TokenId.fromProtobuf(tokenAssociation.getTokenId()) : new TokenId(0, 0, 0),
            tokenAssociation.hasAccountId() ? AccountId.fromProtobuf(tokenAssociation.getAccountId()) : new AccountId(0, 0, 0)
        );
    }

    public static TokenAssociation fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TokenAssociation.parseFrom(bytes));
    }

    com.hedera.hashgraph.sdk.proto.TokenAssociation toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.TokenAssociation.newBuilder()
            .setTokenId(tokenId.toProtobuf())
            .setAccountId(accountId.toProtobuf())
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("tokenId", tokenId)
            .add("accountId", accountId)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
