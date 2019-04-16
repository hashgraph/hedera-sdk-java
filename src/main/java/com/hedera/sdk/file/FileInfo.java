package com.hedera.sdk.file;

import com.hedera.sdk.TimestampHelper;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.FileGetInfoResponse;
import com.hedera.sdk.proto.Response;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class FileInfo {
    private final FileGetInfoResponse.FileInfo inner;

    FileInfo(Response response) {
        if (!response.hasFileGetInfo())
            throw new IllegalArgumentException("response was not `fileGetInfo`");

        inner = response.getFileGetInfo()
            .getFileInfo();

        if (!inner.hasKeys() || inner.getKeys()
            .getKeysList()
            .isEmpty())
            throw new IllegalArgumentException("`FileGetInfoResponse` missing keys");
    }

    public FileId getFileId() {
        return new FileId(inner.getFileIDOrBuilder());
    }

    public long getSize() {
        return inner.getSize();
    }

    public Instant getExpirationTime() {
        return TimestampHelper.timestampTo(inner.getExpirationTime());
    }

    public boolean isDeleted() {
        return inner.getDeleted();
    }

    public List<Key> getKeys() {
        return inner.getKeys()
            .getKeysList()
            .stream()
            .map(Key::fromProtoKey)
            .collect(Collectors.toList());
    }
}
