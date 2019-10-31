package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.FileGetInfoResponse;
import com.hederahashgraph.api.proto.java.Response;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class FileInfo {
    private final FileGetInfoResponse.FileInfo inner;

    FileInfo(Response response) {
        if (!response.hasFileGetInfo()) throw new IllegalArgumentException("response was not `fileGetInfo`");

        inner = response.getFileGetInfo()
            .getFileInfo();

        if (!inner.hasKeys() || inner.getKeys().getKeysList().isEmpty()) {
            throw new IllegalArgumentException("`FileGetInfoResponse` missing keys");
        }
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

    public List<PublicKey> getKeys() {
        return inner.getKeys()
            .getKeysList()
            .stream()
            .map(PublicKey::fromProtoKey)
            .collect(Collectors.toList());
    }
}
