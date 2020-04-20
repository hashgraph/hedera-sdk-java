package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileID;

import javax.annotation.Nonnegative;

public final class FileId extends EntityId {
    /**
     * The public node address book for the current network.
     *
     * This file can be decoded using {@link com.hedera.hashgraph.sdk.proto.NodeAddressBook}.
     */
    public static final FileId ADDRESS_BOOK = new FileId(0, 0, 102);

    public FileId(@Nonnegative long num) {
        super(0, 0, num);
    }

    @SuppressWarnings("InconsistentOverloads")
    public FileId(@Nonnegative long shard, @Nonnegative long realm, @Nonnegative long num) {
        super(shard, realm, num);
    }

    public static FileId fromString(String id) {
        return EntityId.fromString(id, FileId::new);
    }

    static FileId fromProtobuf(FileID fileId) {
        return new FileId(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum());
    }

    FileID toProtobuf() {
        return FileID.newBuilder().setShardNum(shard).setRealmNum(realm).setFileNum(num).build();
    }

    byte[] toBytes() {
        return this.toProtobuf().toByteArray();
    }

    FileId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(FileID.parseFrom(bytes).toBuilder().build());
    }
}
