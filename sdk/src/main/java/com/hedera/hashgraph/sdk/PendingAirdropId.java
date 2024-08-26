package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import javax.annotation.Nullable;

public class PendingAirdropId {
    private final AccountId sender;
    private final AccountId receiver;
    @Nullable
    private final TokenId tokenId;
    @Nullable
    private final NftId nftId;

    public PendingAirdropId(AccountId sender, AccountId receiver, TokenId tokenId) {
        this.sender = sender;
        this.receiver = receiver;
        this.tokenId = tokenId;
        this.nftId = null;
    }
    public PendingAirdropId(AccountId sender, AccountId receiver, NftId nftId) {
        this.sender = sender;
        this.receiver = receiver;
        this.nftId = nftId;
        this.tokenId = null;
    }

    public AccountId getSender() {
        return sender;
    }


    public AccountId getReceiver() {
        return receiver;
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public NftId getNftId() {
        return nftId;
    }

    static PendingAirdropId fromProtobuf(com.hedera.hashgraph.sdk.proto.PendingAirdropId pendingAirdropId) {
        if (pendingAirdropId.hasFungibleTokenType()) {
            return new PendingAirdropId(AccountId.fromProtobuf(pendingAirdropId.getSenderId()),
                AccountId.fromProtobuf(pendingAirdropId.getReceiverId()),
                TokenId.fromProtobuf(pendingAirdropId.getFungibleTokenType()));
        } else {
            return new PendingAirdropId(AccountId.fromProtobuf(pendingAirdropId.getSenderId()),
                AccountId.fromProtobuf(pendingAirdropId.getReceiverId()),
                NftId.fromProtobuf(pendingAirdropId.getNonFungibleToken()));
        }
    }

    com.hedera.hashgraph.sdk.proto.PendingAirdropId toProtobuf() {
        var builder = com.hedera.hashgraph.sdk.proto.PendingAirdropId.newBuilder()
            .setSenderId(sender.toProtobuf())
            .setReceiverId(receiver.toProtobuf());

        if (tokenId != null) {
            builder.setFungibleTokenType(tokenId.toProtobuf());
        } else if (nftId != null) {
            builder.setNonFungibleToken(nftId.toProtobuf());
        }
        return builder.build();
     }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("sender",  sender)
            .add("receiver",  receiver)
            .add("tokenId",  tokenId)
            .add("nftId",  nftId)
            .toString();
    }
}
