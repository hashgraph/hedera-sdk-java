/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
import java.util.Objects;

/**
 * Get the contents of a file. The content field is empty (no bytes) if the file is empty.
 */
public final class FileContentsQuery extends Query<ByteString, FileContentsQuery> {

    @Nullable
    private FileId fileId = null;

    public FileContentsQuery() {
    }

    @Nullable
    public FileId getFileId() {
        return fileId;
    }

    /**
     * Sets the file ID of the file whose contents are requested.
     *
     * @param fileId The FileId to be set
     * @return {@code this}
     */
    public FileContentsQuery setFileId(FileId fileId) {
        Objects.requireNonNull(fileId);
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
    void validateChecksums(Client client) throws BadEntityIdException {
        if (fileId != null) {
            fileId.validateChecksum(client);
        }
    }

    @Override
    void onMakeRequest(com.hedera.hashgraph.sdk.proto.Query.Builder queryBuilder, QueryHeader header) {
        var builder = FileGetContentsQuery.newBuilder();
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
