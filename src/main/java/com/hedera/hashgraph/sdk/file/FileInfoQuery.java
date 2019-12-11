package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.QueryBuilder;
import com.hedera.hashgraph.proto.FileGetInfoQuery;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.proto.FileServiceGrpc;

import io.grpc.MethodDescriptor;

// `FileGetInfoQuery`
public class FileInfoQuery extends QueryBuilder<FileInfo, FileInfoQuery> {
    private final FileGetInfoQuery.Builder builder = inner.getFileGetInfoBuilder();

    /**
     * @deprecated {@link Client} should now be provided to {@link #execute(Client)}
     */
    @Deprecated
    public FileInfoQuery(Client client) {
        super(client);
    }

    public FileInfoQuery() {
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
        return FileInfo.fromResponse(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
