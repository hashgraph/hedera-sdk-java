package com.hedera.hashgraph.sdk;

import com.google.protobuf.ByteString;
import com.hedera.hashgraph.sdk.proto.FileGetContentsQuery;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.QueryHeader;
import com.hedera.hashgraph.sdk.proto.Response;
import com.hedera.hashgraph.sdk.proto.ResponseHeader;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;

import javax.annotation.Nullable;

/**
 * Get the contents of a file. The content field is empty (no bytes) if the file is empty.
 */
public final class FileContentsQuery extends Query<ByteString, FileContentsQuery> {
    private final FileGetContentsQuery.Builder builder;

    FileId fileId;

    public FileContentsQuery() {
        this.builder = FileGetContentsQuery.newBuilder();
    }

    public FileId getFileId() {
        return fileId;
    }

    /**
     * Sets the file ID of the file whose contents are requested.
     *
     * @return {@code this}
     * @param fileId The FileId to be set
     */
    public FileContentsQuery setFileId(FileId fileId) {
        this.fileId = fileId;
        return this;
    }

    @Override
    public CompletableFuture<Hbar> getCostAsync(Client client) {
        // deleted accounts return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 25 tinybar seems to be enough to get
        // `FILE_DELETED` back instead.
        return super.getCostAsync(client).thenApply((cost) -> Hbar.fromTinybars(Math.max(cost.toTinybars(), 25)));
    }

    @Override
    void validateNetworkOnIds(@Nullable AccountId accountId) {
        EntityIdHelper.validateNetworkOnIds(this.fileId, accountId);
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }

        queryBuilder.setFileGetContents(builder.setHeader(header));
    }

    @Override
    ResponseHeader mapResponseHeader(Response response) {
        return response.getFileGetContents().getHeader();
    }

    @Override
    QueryHeader mapRequestHeader(com.hedera.hashgraph.sdk.proto.Query request) {
        return request.getFileGetContents().getHeader();
    }

    @Override
    ByteString mapResponse(Response response, AccountId nodeId, com.hedera.hashgraph.sdk.proto.Query request) {
        return response.getFileGetContents().getFileContents().getContents();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Query, Response> getMethodDescriptor() {
        return FileServiceGrpc.getGetFileContentMethod();
    }
}
