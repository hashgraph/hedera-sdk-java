package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.SolidityUtil;
import com.hederahashgraph.api.proto.java.FileID;
import com.hederahashgraph.api.proto.java.FileIDOrBuilder;

import java.util.Objects;

public final class FileId implements Entity {
    public final long shard;
    public final long realm;
    public final long file;

    public FileId(long shardNum, long realmNum, long fileNum) {
        this.shard = shardNum;
        this.realm = realmNum;
        this.file = fileNum;
    }

    public FileId(FileIDOrBuilder fileId) {
        this(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum());
    }

    /** Constructs a `FileId` from a string formatted as <shardNum>.<realmNum>.<fileNum> */
    public static FileId fromString(String account) throws IllegalArgumentException {
        return IdUtil.parseIdString(account, FileId::new);
    }

    public static FileId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, FileId::new);
    }

    @Deprecated
    public long getShardNum() {
        return shard;
    }

    @Deprecated
    public long getRealmNum() {
        return realm;
    }

    @Deprecated
    public long getFileNum() {
        return file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(shard, realm, file);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (!(other instanceof FileId)) return false;

        FileId otherId = (FileId) other;
        return shard == otherId.shard && realm == otherId.realm && file == otherId.file;
    }

    public FileID toProto() {
        return FileID.newBuilder()
            .setShardNum(shard)
            .setRealmNum(realm)
            .setFileNum(file)
            .build();
    }

    @Override
    public String toString() {
        return "" + shard + "." + realm + "." + file;
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }
}
