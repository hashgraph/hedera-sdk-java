package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;

public class PendingAirdropId {
    private AccountId sender;
    private AccountId receiver;
    private TokenId tokenId;
    private NftId nftId;

    public PendingAirdropId(AccountId sender, AccountId receiver, TokenId tokenId, NftId nftId) {
        this.sender = sender;
        this.receiver = receiver;
        this.tokenId = tokenId;
        this.nftId = nftId;
    }

    public PendingAirdropId() {
    }

    public void setSender(final AccountId sender) {
        this.sender = sender;
    }
    public AccountId getSender() {
        return sender;
    }

    public void setReceiver(AccountId receiver) {
        this.receiver = receiver;
    }

    public AccountId getReceiver() {
        return receiver;
    }

    public void setTokenId(TokenId tokenId) {
        this.tokenId = tokenId;
    }

    public TokenId getTokenId() {
        return tokenId;
    }

    public void setNftId(NftId nftId) {
        this.nftId = nftId;
    }

    public NftId getNftId() {
        return nftId;
    }

    static PendingAirdropId fromProtobuf(com.hedera.hashgraph.sdk.proto.PendingAirdropId pendingAirdropId) {
        return new PendingAirdropId(AccountId.fromProtobuf(pendingAirdropId.getSenderId()),
            AccountId.fromProtobuf(pendingAirdropId.getReceiverId()),
            TokenId.fromProtobuf(pendingAirdropId.getFungibleTokenType()),
            NftId.fromProtobuf(pendingAirdropId.getNonFungibleToken()));
    }

    com.hedera.hashgraph.sdk.proto.PendingAirdropId toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.PendingAirdropId.newBuilder()
            .setFungibleTokenType(tokenId.toProtobuf())
            .setNonFungibleToken(nftId.toProtobuf())
            .setSenderId(sender.toProtobuf())
            .setReceiverId(receiver.toProtobuf())
            .build();
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
