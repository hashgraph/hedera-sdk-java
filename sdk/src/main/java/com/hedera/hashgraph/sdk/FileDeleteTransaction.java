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

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileDeleteTransactionBody;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * <p>A transaction to delete a file on the Hedera network.
 *
 * <p>When deleted, a file's contents are truncated to zero length and it can no longer be updated
 * or appended to, or its expiration time extended. {@link FileContentsQuery} and {@link FileInfoQuery}
 * will throw {@link PrecheckStatusException} with a status of {@link Status#FILE_DELETED}.
 *
 * <p>Only one of the file's keys needs to sign to delete the file, unless the key you have is part
 * of a {@link com.hedera.hashgraph.sdk.KeyList}.
 */
public final class FileDeleteTransaction extends Transaction<FileDeleteTransaction> {

    @Nullable
    private FileId fileId = null;

    public FileDeleteTransaction() {
    }

    FileDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    FileDeleteTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public FileId getFileId() {
        return fileId;
    }

    /**
     * <p>Set the ID of the file to delete. Required.
     *
     * @param fileId the ID of the file to delete.
     * @return {@code this}
     */
    public FileDeleteTransaction setFileId(FileId fileId) {
        Objects.requireNonNull(fileId);
        requireNotFrozen();
        this.fileId = fileId;
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getFileDelete();
        if (body.hasFileID()) {
            fileId = FileId.fromProtobuf(body.getFileID());
        }
    }

    FileDeleteTransactionBody.Builder build() {
        var builder = FileDeleteTransactionBody.newBuilder();
        if (fileId != null) {
            builder.setFileID(fileId.toProtobuf());
        }

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (fileId != null) {
            fileId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getDeleteFileMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileDelete(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFileDelete(build());
    }
}
