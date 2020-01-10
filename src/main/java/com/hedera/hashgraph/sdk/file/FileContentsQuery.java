package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.FileGetContentsQuery;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.QueryBuilder;

import io.grpc.MethodDescriptor;

/**
 * Get the contents of a file.
 */
// `FileGetContentsQuery`
public class FileContentsQuery extends QueryBuilder<byte[], FileContentsQuery> {
    private final FileGetContentsQuery.Builder builder = inner.getFileGetContentsBuilder();

    public FileContentsQuery() {
        super();
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
    protected byte[] extractResponse(Response raw) {
        return raw.getFileGetContents().toByteArray();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
