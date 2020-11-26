package com.hedera.hashgraph.sdk;

import com.google.errorprone.annotations.Var;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.*;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hederahashgraph.api.proto.java.TransactionList;
import io.grpc.MethodDescriptor;
import java8.util.concurrent.CompletableFuture;
import java8.util.function.Function;

import javax.annotation.Nullable;
import java.util.*;

/**
 * <p>A transaction specifically to append data to a file on the network.
 *
 * <p>If a file has multiple keys, all keys must sign to modify its contents.
 * (See {@link FileCreateTransaction#setKeys(Key...)} for more information.)
 */
public final class FileAppendTransaction extends Transaction<FileAppendTransaction> implements WithExecuteAll {
    private static final int CHUNK_SIZE = 4096;

    private final FileAppendTransactionBody.Builder builder;

    /**
     * Maximum number of chunks this message will get broken up into when
     * its frozen.
     */
    private int maxChunks = 10;

    private ByteString contents;

    public FileAppendTransaction() {
        builder = FileAppendTransactionBody.newBuilder();
    }

    FileAppendTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);

        builder = bodyBuilder.getFileAppend().toBuilder();

        for (var i = 0; i < signedTransactions.size(); i += nodeIds.size()) {
            contents = contents.concat(
                TransactionBody.parseFrom(signedTransactions.get(i).getBodyBytes())
                    .getFileAppend().getContents()
            );
        }
    }

    @Nullable
    public FileId getFileId() {
        return builder.hasFileID() ? FileId.fromProtobuf(builder.getFileID()) : null;
    }

    /**
     * <p>Set the ID of the file to append to. Required.
     *
     * @param fileId the ID of the file to append to.
     * @return {@code this}
     */
    public FileAppendTransaction setFileId(FileId fileId) {
        requireNotFrozen();
        builder.setFileID(fileId.toProtobuf());
        return this;
    }

    @Nullable
    public ByteString getContents() {
        return contents;
    }

    /**
     * <p>Set the contents to append to the file as identified by {@link #setFileId(FileId)}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param contents the contents to append to the file.
     * @return {@code this}
     * @see #setContents(String) for an overload which takes {@link String}.
     */
    public FileAppendTransaction setContents(byte[] contents) {
        requireNotFrozen();
        this.contents = ByteString.copyFrom(contents);
        return this;
    }

    /**
     * <p>Encode the given {@link String} as UTF-8 and append it to file as identified by
     * {@link #setFileId(FileId)}.
     *
     * <p>If the whole file is UTF-8 encoded, the string can later be recovered from
     * {@link FileContentsQuery#execute(Client)} via
     * {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link com.hedera.hashgraph.sdk.HederaPreCheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>If you want to append more than ~6KiB of data, you will need to break it into multiple chunks
     * and use a separate {@link FileAppendTransaction} for each.
     *
     * @param text The String to be set as the contents of the file
     * @return {@code this}
     * @see #setContents(byte[]) for appending arbitrary data.
     */
    public FileAppendTransaction setContents(String text) {
        requireNotFrozen();
        this.contents = ByteString.copyFromUtf8(text);
        return this;
    }

    public FileAppendTransaction setMaxChunks(int maxChunks) {
        requireNotFrozen();
        this.maxChunks = maxChunks;
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getAppendContentMethod();
    }

    @Override
    public FileAppendTransaction freezeWith(@Nullable Client client) {
        super.freezeWith(client);

        var initialTransactionId = Objects.requireNonNull(transactionIds.get(0)).toProtobuf();
        var requiredChunks = (this.contents.size() + (CHUNK_SIZE - 1)) / CHUNK_SIZE;

        if (requiredChunks == 1) {
            return this;
        }

        if (requiredChunks > maxChunks) {
            throw new IllegalArgumentException(
                "message of " + this.contents.size() + " bytes requires " + requiredChunks
                    + " chunks but the maximum allowed chunks is " + maxChunks + ", try using setMaxChunks");
        }

        signatures = new ArrayList<>(requiredChunks * nodeIds.size());
        transactions = new ArrayList<>(requiredChunks * nodeIds.size());
        signedTransactions = new ArrayList<>(requiredChunks * nodeIds.size());
        transactionIds = new ArrayList<>(requiredChunks);

        @Var var nextTransactionId = initialTransactionId.toBuilder();

        for (int i = 0; i < requiredChunks; i++) {
            @Var var startIndex = i * CHUNK_SIZE;
            @Var var endIndex = startIndex + CHUNK_SIZE;

            if (endIndex > this.contents.size()) {
                endIndex = this.contents.size();
            }

            transactionIds.add(TransactionId.fromProtobuf(nextTransactionId.build()));

            bodyBuilder
                .setTransactionID(nextTransactionId.build())
                .setFileAppend(
                    builder
                        .setContents(contents.substring(startIndex, endIndex))
                        .build()
                );

            // For each node we add a transaction with that node
            for (var nodeId : nodeIds) {
                signatures.add(SignatureMap.newBuilder());
                signedTransactions.add(com.hedera.hashgraph.sdk.proto.SignedTransaction.newBuilder()
                    .setBodyBytes(
                        bodyBuilder
                            .setNodeAccountID(nodeId.toProtobuf())
                            .build()
                            .toByteString()
                    )
                );
            }

            // add 1 ns to the validStart to make cascading transaction IDs
            var nextValidStart = nextTransactionId.getTransactionValidStart().toBuilder();
            nextValidStart.setNanos(nextValidStart.getNanos() + 1);

            nextTransactionId.setTransactionValidStart(nextValidStart);
        }

        return this;
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        return true;
    }

    @Override
    public CompletableFuture<com.hedera.hashgraph.sdk.TransactionResponse> executeAsync(Client client) {
        return executeAllAsync(client).thenApply(responses -> responses.get(0));
    }

    @Override
    public CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> executeAllAsync(Client client) {
        if (!isFrozen()) {
            freezeWith(client);
        }

        var operatorId = client.getOperatorAccountId();

        if (operatorId != null && operatorId.equals(Objects.requireNonNull(getTransactionId()).accountId)) {
            // on execute, sign each transaction with the operator, if present
            // and we are signing a transaction that used the default transaction ID
            signWithOperator(client);
        }

        CompletableFuture<List<com.hedera.hashgraph.sdk.TransactionResponse>> future =
            CompletableFuture.supplyAsync(() -> new ArrayList<>(transactionIds.size()));

        for (var i = 0; i < transactionIds.size(); i++) {
            future = future.thenCompose(list -> super.executeAsync(client).thenApply(response -> {
                    list.add(response);
                    return list;
                }).thenCompose(CompletableFuture::completedFuture)
            );
        }

        return future;
    }
}
