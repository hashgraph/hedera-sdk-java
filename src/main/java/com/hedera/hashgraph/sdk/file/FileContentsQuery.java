package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.sdk.proto.*;
import io.grpc.MethodDescriptor;

// `FileGetContentsQuery`
public class FileContentsQuery extends QueryBuilder<FileGetContentsResponse> {
    private final FileGetContentsQuery.Builder builder = inner.getFileGetContentsBuilder();

    public FileContentsQuery(Client client) {
        super(client);
    }

    FileContentsQuery() {
        super(null);
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

    @Override
    protected FileGetContentsResponse fromResponse(Response raw) {
        return raw.getFileGetContents();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
