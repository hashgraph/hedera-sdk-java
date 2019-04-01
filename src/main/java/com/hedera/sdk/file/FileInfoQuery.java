package com.hedera.sdk.file;

import com.hedera.sdk.FileId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `FileGetInfoQuery`
public class FileInfoQuery extends QueryBuilder<FileGetInfoResponse> {
    private final com.hedera.sdk.proto.FileGetInfoQuery.Builder builder;

    public FileInfoQuery() {
        super(Response::getFileGetInfo);
        builder = inner.getFileGetInfoBuilder();
    }

    @Override
    protected QueryHeader.Builder getHeaderBuilder() {
        return builder.getHeaderBuilder();
    }

    public FileInfoQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.toProto());
        return this;
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethod() {
        return FileServiceGrpc.getGetFileInfoMethod();
    }

    @Override
    protected void doValidate() {
        require(builder.getFileIDOrBuilder(), ".setFileId()");
    }
}
