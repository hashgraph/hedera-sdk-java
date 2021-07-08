package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;

class TokenNftTransfer {
    public final AccountId sender;
    public final AccountId receiver;
    public final long serial;

    TokenNftTransfer(AccountId sender, AccountId receiver, long serial) {
        this.sender = sender;
        this.receiver = receiver;
        this.serial = serial;
    }

    static TokenNftTransfer fromProtobuf(com.hedera.hashgraph.sdk.proto.NftTransfer nftTransfer, @Nullable NetworkName networkName) {
        return new TokenNftTransfer(
            AccountId.fromProtobuf(nftTransfer.getSenderAccountID()),
            AccountId.fromProtobuf(nftTransfer.getReceiverAccountID()),
            nftTransfer.getSerialNumber()
        );
    }

    static TokenNftTransfer fromProtobuf(com.hedera.hashgraph.sdk.proto.NftTransfer info) {
        return fromProtobuf(info, null);
    }

    public static TokenNftTransfer fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.NftTransfer.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.NftTransfer toProtobuf() {
        return com.hedera.hashgraph.sdk.proto.NftTransfer.newBuilder()
            .setSenderAccountID(sender.toProtobuf())
            .setReceiverAccountID(receiver.toProtobuf())
            .setSerialNumber(serial)
            .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("sender", sender)
            .add("receiver", receiver)
            .add("serial", serial)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
};
