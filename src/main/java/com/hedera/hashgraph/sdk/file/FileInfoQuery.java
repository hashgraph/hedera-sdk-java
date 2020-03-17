package com.hedera.hashgraph.sdk.file;

import com.hedera.hashgraph.proto.FileGetInfoQuery;
import com.hedera.hashgraph.proto.FileServiceGrpc;
import com.hedera.hashgraph.proto.Query;
import com.hedera.hashgraph.proto.QueryHeader;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaNetworkException;
import com.hedera.hashgraph.sdk.HederaStatusException;
import com.hedera.hashgraph.sdk.HederaThrowable;
import com.hedera.hashgraph.sdk.QueryBuilder;

import java.util.function.Consumer;

import io.grpc.MethodDescriptor;

// `FileGetInfoQuery`
public class FileInfoQuery extends QueryBuilder<FileInfo, FileInfoQuery> {
    private final FileGetInfoQuery.Builder builder = inner.getFileGetInfoBuilder();

    public FileInfoQuery() {
        super();
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
    protected FileInfo extractResponse(Response raw) {
        return FileInfo.fromResponse(raw);
    }

    @Override
    protected void doValidate() {
        require(builder.hasFileID(), ".setFileId() required");
    }

    @Override
    public long getCost(Client client) throws HederaStatusException, HederaNetworkException {
        // deleted files return a COST_ANSWER of zero which triggers `INSUFFICIENT_TX_FEE`
        // if you set that as the query payment; 50 tinybar seems to be the minimum to get
        // `FILE_DELETED` back instead.
        return Math.max(super.getCost(client), 50);
    }

    @Override
    public void getCostAsync(Client client, Consumer<Long> withCost, Consumer<HederaThrowable> onError) {
        // see above
        super.getCostAsync(client, (cost) -> withCost.accept(Math.min(cost, 25)), onError);
    }
}
