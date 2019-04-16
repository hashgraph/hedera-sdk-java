package com.hedera.sdk.file;

import com.hedera.sdk.Client;
import com.hedera.sdk.HederaException;
import com.hedera.sdk.QueryBuilder;
import com.hedera.sdk.proto.*;
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
    protected FileGetContentsResponse mapResponse(Response raw) throws HederaException {
        return raw.getFileGetContents();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
