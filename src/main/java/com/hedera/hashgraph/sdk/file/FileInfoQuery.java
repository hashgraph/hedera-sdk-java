package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `FileGetInfoQuery`
public class FileInfoQuery extends QueryBuilder<FileInfo> {
    private final FileGetInfoQuery.Builder builder = inner.getFileGetInfoBuilder();

    public FileInfoQuery(Client client) {
        super(client);
    }

    FileInfoQuery() {
        super(null);
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
    protected FileInfo fromResponse(Response raw) {
        return new FileInfo(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
