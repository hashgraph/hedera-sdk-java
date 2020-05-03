package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileGetInfoResponse;
import org.threeten.bp.Instant;

import java.util.ArrayList;
import java.util.List;

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
    public final boolean deleted;

    /**
     * One of these keys must sign in order to delete the file.
     * All of these keys must sign in order to update the file.
     */
    public final List<Key> keys;

    private FileInfo(
        FileId fileId,
        long size,
        Instant expirationTime,
        boolean deleted,
        List<Key> keys
    ) {
        this.fileId = fileId;
        this.size = size;
        this.expirationTime = expirationTime;
        this.deleted = deleted;
        this.keys = keys;
    }

    static FileInfo fromProtobuf(FileGetInfoResponse.FileInfo fileInfo) {
        var keys = new ArrayList<Key>(fileInfo.getKeys().getKeysCount());
        for (var i = 0; i < fileInfo.getKeys().getKeysCount(); ++i) {
            keys.add(Key.fromProtobuf(fileInfo.getKeys().getKeys(i)));
        }

        return new FileInfo(
            FileId.fromProtobuf(fileInfo.getFileID()),
            fileInfo.getSize(),
            InstantConverter.fromProtobuf(fileInfo.getExpirationTime()),
            fileInfo.getDeleted(),
            keys
        );
    }

    public static FileInfo fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(FileGetInfoResponse.FileInfo.parseFrom(bytes).toBuilder().build());
    }

    FileGetInfoResponse.FileInfo toProtobuf() {
        var keyList = com.hedera.hashgraph.sdk.proto.KeyList.newBuilder();
        for (Key key : keys) {
            keyList.addKeys(key.toKeyProtobuf());
        }

        var fileInfoBuilder = FileGetInfoResponse.FileInfo.newBuilder()
            .setFileID(this.fileId.toProtobuf())
            .setSize(this.size)
            .setExpirationTime(InstantConverter.toProtobuf(this.expirationTime))
            .setDeleted(this.deleted)
            .setKeys(keyList);

        return fileInfoBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("fileId", fileId)
            .add("size", size)
            .add("expirationTime", expirationTime)
            .add("deleted", deleted)
            .add("keys", keys)
            .toString();
    }

    public byte[] toBytes() {
        return this.toProtobuf().toByteArray();
    }
}
