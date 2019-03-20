package com.hedera.sdk;

import com.hedera.sdk.proto.FileID;

public class FileId {
    transient FileID.Builder inner;

    public FileId(long shardNum, long realmNum, long fileNum) {
        inner = FileID.newBuilder()
            .setShardNum(shardNum)
            .setRealmNum(realmNum)
            .setFileNum(fileNum);
    }
}
