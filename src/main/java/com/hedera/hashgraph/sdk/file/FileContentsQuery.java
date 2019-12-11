package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.FileGetContentsQuery;
import com.hedera.hashgraph.proto.FileGetContentsResponse;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.FileServiceGrpc;

import io.grpc.MethodDescriptor;

// `FileGetContentsQuery`
public class FileContentsQuery extends QueryBuilder<FileGetContentsResponse, FileContentsQuery> {
    private final FileGetContentsQuery.Builder builder = inner.getFileGetContentsBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public FileContentsQuery(Client client) {
        super(client);
    }

    public FileContentsQuery() {
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
