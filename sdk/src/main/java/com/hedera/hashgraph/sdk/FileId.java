package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FileID;

import javax.annotation.Nonnegative;

public final class FileId extends EntityId {
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
}
