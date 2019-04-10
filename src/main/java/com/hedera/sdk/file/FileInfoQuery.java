package com.hedera.sdk.file;

import com.hedera.sdk.Client;
import com.hedera.sdk.FileId;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `FileGetInfoQuery`
public class FileInfoQuery extends QueryBuilder<FileInfo> {
    private final FileGetInfoQuery.Builder builder = inner.getFileGetInfoBuilder();

    public FileInfoQuery(Client client) {
        super(client, FileInfo::new);
    }

    FileInfoQuery() {
        super(null, FileInfo::new);
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
        require(builder.hasFileID(), ".setFileId() required");
    }
}
