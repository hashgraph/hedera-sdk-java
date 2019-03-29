package com.hedera.sdk.file;

import com.hedera.sdk.FileId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `FileGetContentsQuery`
public class FileContentsQuery extends QueryBuilder<FileGetContentsResponse> {
    private final com.hedera.sdk.proto.FileGetContentsQuery.Builder builder;

    public FileContentsQuery() {
        super(Response::getFileGetContents);
        builder = inner.getFileGetContentsBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public FileContentsQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return FileServiceGrpc.getGetFileContentMethod();
    }
}
