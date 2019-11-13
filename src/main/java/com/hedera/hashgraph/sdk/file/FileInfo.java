package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.FileGetInfoResponse;
import com.hederahashgraph.api.proto.java.Response;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class FileInfo {
    public final FileId fileId;
    public final long size;
    public final Instant expirationTime;
    public final boolean isDeleted;
    public final List<PublicKey> keys;

    public FileInfo(FileGetInfoResponse.FileInfoOrBuilder info) {
        if (!info.hasKeys() || info.getKeys().getKeysList().isEmpty()) {
            throw new IllegalArgumentException("`FileGetInfoResponse` missing keys");
        }

        fileId = new FileId(info.getFileIDOrBuilder());
        size = info.getSize();
        expirationTime = TimestampHelper.timestampTo(info.getExpirationTime());
        isDeleted = info.getDeleted();
        keys = info.getKeys()
            .getKeysList()
            .stream()
            .map(PublicKey::fromProtoKey)
            .collect(Collectors.toList());
    }

    static FileInfo fromResponse(Response response) {
        if (!response.hasFileGetInfo()) throw new IllegalArgumentException("response was not `fileGetInfo`");

        return new FileInfo(response.getFileGetInfo().getFileInfoOrBuilder());
    }

    @Deprecated
    public FileId getFileId() {
        return fileId;
    }

    @Deprecated
    public long getSize() {
        return size;
    }

    @Deprecated
    public Instant getExpirationTime() {
        return expirationTime;
    }

    @Deprecated
    public boolean isDeleted() {
        return isDeleted;
    }

    @Deprecated
    public List<PublicKey> getKeys() {
        return keys;
    }
}
