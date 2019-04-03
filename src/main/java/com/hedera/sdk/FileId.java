package com.hedera.sdk;

import com.hedera.sdk.proto.FileID;
import com.hedera.sdk.proto.FileIDOrBuilder;

import java.util.Objects;

public final class FileId implements Entity {
    transient FileID.Builder inner;

    public FileId(long shardNum, long realmNum, long fileNum) {
        inner = FileID.newBuilder()
            .setShardNum(shardNum)
            .setRealmNum(realmNum)
            .setFileNum(fileNum);
    }

    public FileId(FileIDOrBuilder fileId) {
        inner = FileID.newBuilder()
            .setShardNum(fileId.getShardNum())
            .setRealmNum(fileId.getRealmNum())
            .setFileNum(fileId.getFileNum());
    }

    public long getShardNum() {
        return inner.getShardNum();
    }

    public long getRealmNum() {
        return inner.getRealmNum();
    }

    public long getFileNum() {
        return inner.getFileNum();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getShardNum(), getRealmNum(), getFileNum());
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof FileId))
            return false;
        var fileId = (FileId) other;
        return getShardNum() == fileId.getShardNum() && getRealmNum() == fileId.getRealmNum() && getFileNum() == fileId.getFileNum();
    }

    public FileID toProto() {
        return inner.build();
    }
}
