package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.FileGetContentsQuery;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class FileContentsQuery extends QueryBuilder<ByteString, FileContentsQuery> {
    private final FileGetContentsQuery.Builder builder;

    public FileContentsQuery() {
        this.builder = FileGetContentsQuery.newBuilder();
    }

    public FileContentsQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setFileGetContents(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getFileGetContents().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getFileGetContents().getHeader();
    }

    @Override
    protected ByteString mapResponse(Response response) {
        return response.getFileGetContents().getFileContents().getContents();
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return FileServiceGrpc.getGetFileContentMethod();
    }
}
