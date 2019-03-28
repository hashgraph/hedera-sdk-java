package com.hedera.sdk;

import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

public class FileGetInfoQuery extends QueryBuilder<FileGetInfoResponse> {
    private final com.hedera.sdk.proto.FileGetInfoQuery.Builder builder;

    public FileGetInfoQuery() {
        super(Response::getFileGetInfo);
        builder = inner.getFileGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public FileGetInfoQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.inner);
        return this;
    }

    @Override
    MethodDescriptor<Query, Response> getMethod() {
        return FileServiceGrpc.getGetFileInfoMethod();
    }
}
