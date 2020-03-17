package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.FileGetContentsQuery;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
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
    public long getCost(Client client) throws HederaStatusException, HederaNetworkException {
        // deleted files return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 50 tinybar seems to be the minimum to get
        // `FILE_DELETED` back instead.
        return Math.max(super.getCost(client), 50);
    }

    @Override
    protected byte[] extractResponse(Response raw) {
        return raw.getFileGetContents().getFileContents().getContents().toByteArray();
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }
}
