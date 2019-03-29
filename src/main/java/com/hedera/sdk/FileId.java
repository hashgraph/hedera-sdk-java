package com.hedera.sdk;

import com.hedera.sdk.proto.FileID;

public class FileId {
    transient FileID.Builder inner;

    public FileId(long shardNum, long realmNum, long fileNum) {
        inner = FileID.newBuilder().setShardNum(shardNum).setRealmNum(realmNum).setFileNum(fileNum);
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

    FileID toProto() {
        return inner.build();
    }
}
