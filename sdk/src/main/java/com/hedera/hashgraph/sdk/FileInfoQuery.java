package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.FileGetInfoQuery;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.Query;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;

public final class FileInfoQuery extends QueryBuilder<FileInfo, FileInfoQuery> {
    private final FileGetInfoQuery.Builder builder;

    public FileInfoQuery() {
        builder = FileGetInfoQuery.newBuilder();
    }

    public FileInfoQuery setFileId(FileId fileId) {
        builder.setFileID(fileId.toProtobuf());

        return this;
    }

    @Override
    protected void onMakeRequest(Query.Builder queryBuilder, QueryHeader header) {
        queryBuilder.setFileGetInfo(builder.setHeader(header));
    }

    @Override
    protected ResponseHeader mapResponseHeader(Response response) {
        return response.getFileGetInfo().getHeader();
    }

    @Override
    protected QueryHeader mapRequestHeader(Query request) {
        return request.getFileGetInfo().getHeader();
    }

    @Override
    protected FileInfo mapResponse(Response response) {
        return FileInfo.fromProtobuf(response.getFileGetInfo().getFileInfo());
    }

    @Override
    protected MethodDescriptor<Query, Response> getMethodDescriptor() {
        return FileServiceGrpc.getGetFileInfoMethod();
    }
}
