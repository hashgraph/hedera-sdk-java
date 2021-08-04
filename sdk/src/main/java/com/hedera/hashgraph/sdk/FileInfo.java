package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileGetInfoResponse;

import java.time.Instant;

import javax.annotation.Nullable;

/**
 * Current information for a file, including its size.
 */
public final class FileInfo {
    /**
     * The ID of the file for which information is requested.
     */
    public final FileId fileId;

    /**
     * Number of bytes in contents.
     */
    public final long size;

    /**
     * The current time at which this account is set to expire.
     */
    public final Instant expirationTime;

    /**
     * True if deleted but not yet expired.
     */
    public final boolean isDeleted;

    /**
     * One of these keys must sign in order to delete the file.
     * All of these keys must sign in order to update the file.
     */
    @Nullable
    public final KeyList keys;

    public final String fileMemo;

    private FileInfo(
        FileId fileId,
        long size,
        Instant expirationTime,
        boolean isDeleted,
        @Nullable KeyList keys,
        String fileMemo
    ) {
        this.fileId = fileId;
        this.size = size;
        this.expirationTime = expirationTime;
        this.isDeleted = isDeleted;
        this.keys = keys;
        this.fileMemo = fileMemo;
    }

    static FileInfo fromProtobuf(FileGetInfoResponse.FileInfo fileInfo) {
        @Nullable KeyList keys = fileInfo.hasKeys() ?
            KeyList.fromProtobuf(fileInfo.getKeys(), null) :
            null;

        return new FileInfo(
            FileId.fromProtobuf(fileInfo.getFileID()),
            fileInfo.getSize(),
            InstantConverter.fromProtobuf(fileInfo.getExpirationTime()),
            fileInfo.getDeleted(),
            keys,
            fileInfo.getMemo()
        );
    }

    public static FileInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(FileGetInfoResponse.FileInfo.parseFrom(bytes).toBuilder().build());
    }

    FileGetInfoResponse.FileInfo toProtobuf() {
        var fileInfoBuilder = FileGetInfoResponse.FileInfo.newBuilder()
            .setFileID(fileId.toProtobuf())
            .setSize(size)
            .setExpirationTime(InstantConverter.toProtobuf(expirationTime))
            .setDeleted(isDeleted)
            .setMemo(fileMemo);

        if (keys != null) {
            var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();

            for (Key key : keys) {
                keyList.addKeys(key.toProtobufKey());
            }

            fileInfoBuilder.setKeys(keyList);
        }

        return fileInfoBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fileId", fileId)
            .add("size", size)
            .add("expirationTime", expirationTime)
            .add("isDeleted", isDeleted)
            .add("keys", keys)
            .add("fileMemo", fileMemo)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
