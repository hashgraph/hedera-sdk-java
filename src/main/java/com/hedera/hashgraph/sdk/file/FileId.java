package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Entity;
import com.hedera.hashgraph.sdk.IdUtil;
import com.hedera.hashgraph.sdk.SolidityUtil;
import com.hedera.hashgraph.sdk.proto.FileID;
import com.hedera.hashgraph.sdk.proto.FileIDOrBuilder;

import java.util.Objects;

public final class FileId implements Entity {
    private final FileID.Builder inner;

    public FileId(long shardNum, long realmNum, long fileNum) {
        inner = FileID.newBuilder()
            .setShardNum(shardNum)
            .setRealmNum(realmNum)
            .setFileNum(fileNum);
    }

    public FileId(FileIDOrBuilder fileId) {
        this(fileId.getShardNum(), fileId.getRealmNum(), fileId.getFileNum());
    }

    /** Constructs a `FileId` from a string formatted as <shardNum>.<realmNum>.<contractNum> */
    public static FileId fromString(String account) throws IllegalArgumentException {

        long[] rawNums = IdUtil.parseIdString(account);
        var newFile = FileID.newBuilder();

        try {
            newFile.setRealmNum(rawNums[0]);
            newFile.setShardNum(rawNums[1]);
            newFile.setFileNum(rawNums[2]);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Id format, should be in format {shardNum}.{realmNum}.{fileNum}");
        }

        return new FileId(newFile);
    }

    public static FileId fromSolidityAddress(String address) {
        return SolidityUtil.parseAddress(address, FileId::new);
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
        if (this == other) return true;

        if (!(other instanceof FileId)) return false;

        var otherId = (FileId) other;
        return getShardNum() == otherId.getShardNum() && getRealmNum() == otherId.getRealmNum() && getFileNum() == otherId.getFileNum();
    }

    public FileID toProto() {
        return inner.build();
    }

    @Override
    public String toString() {
        return "" + getShardNum() + "." + getRealmNum() + "." + getFileNum();
    }

    public String toSolidityAddress() {
        return SolidityUtil.addressFor(this);
    }
}
