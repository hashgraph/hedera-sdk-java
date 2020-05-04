package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileID;

import javax.annotation.Nonnegative;

/**
 * The ID for a file on Hedera.
 */
public final class FileId extends EntityId {
    /**
     * The public node address book for the current network.
     */
    public static final FileId ADDRESS_BOOK = new FileId(0, 0, 102);

    /**
     * The current fee schedule for the network.
     */
    public static final FileId FEE_SCHEDULE = new FileId(0, 0, 111);

    /**
     * The current exchange rate of HBAR to USD.
     */
    public static final FileId EXCHANGE_RATES = new FileId(0, 0, 112);

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

    public static FileId fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(FileID.parseFrom(bytes).toBuilder().build());
    }

    static FileId fromProtobuf(FileID fileId) {
        return new FileId(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum());
    }

    FileID toProtobuf() {
        return FileID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setFileNum(num)
            .build();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
