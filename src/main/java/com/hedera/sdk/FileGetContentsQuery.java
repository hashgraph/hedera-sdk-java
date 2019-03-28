package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class FileGetContentsQuery extends QueryBuilder<FileGetContentsResponse> {
    private final com.hedera.sdk.proto.FileGetContentsQuery.Builder builder;

    public FileGetContentsQuery() {
        super(Response::getFileGetContents);
        builder = inner.getFileGetContentsBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public FileGetContentsQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return FileServiceGrpc.getGetFileContentMethod();
    }
}
