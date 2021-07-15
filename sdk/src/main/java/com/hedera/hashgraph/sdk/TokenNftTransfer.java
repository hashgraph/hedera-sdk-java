package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import javax.annotation.Nullable;
import com.hedera.hashgraph.sdk.proto.NftTransfer;

class TokenNftTransfer {
    public final AccountId sender;
    public final AccountId receiver;
    public final long serial;

    TokenNftTransfer(AccountId sender, AccountId receiver, long serial) {
        this.sender = sender;
        this.receiver = receiver;
        this.serial = serial;
    }

    static TokenNftTransfer fromProtobuf(NftTransfer nftTransfer, @Nullable NetworkName networkName) {
        return new TokenNftTransfer(
            AccountId.fromProtobuf(nftTransfer.getSenderAccountID()),
            AccountId.fromProtobuf(nftTransfer.getReceiverAccountID()),
            nftTransfer.getSerialNumber()
        );
    }

    static TokenNftTransfer fromProtobuf(NftTransfer info) {
        return fromProtobuf(info, null);
    }

    public static TokenNftTransfer fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(NftTransfer.parseFrom(bytes).toBuilder().build());
    }

    NftTransfer toProtobuf() {
        return NftTransfer.newBuilder()
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
}
