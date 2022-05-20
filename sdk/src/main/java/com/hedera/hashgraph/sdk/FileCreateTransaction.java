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
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.FileCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.FileServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Creates a file with the content by submitting the transaction.
 * {@link https://docs.hedera.com/guides/getting-started/try-examples/deploy-a-contract-using-the-hedera-token-service#2.-store-the-smart-contract-bytecode-on-hedera}
 */
public final class FileCreateTransaction extends Transaction<FileCreateTransaction> {

    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private KeyList keys = null;
    private byte[] contents = {};
    private String fileMemo = "";

    /**
     * Constructor.
     */
    public FileCreateTransaction() {
        setExpirationTime(Instant.now().plus(DEFAULT_AUTO_RENEW_PERIOD));
        defaultMaxTransactionFee = new Hbar(5);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException
     */   FileCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */   FileCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * @return                          expiration time
     */
    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * <p>Set the instant at which this file will expire, after which its contents will no longer be
     * available.
     *
     * <p>Defaults to 1/4 of a Julian year from the instant {@link #FileCreateTransaction()}
     * was invoked.
     *
     * <p>May be extended using {@link FileUpdateTransaction#setExpirationTime(Instant)}.
     *
     * @param expirationTime the {@link Instant} at which this file should expire.
     * @return {@code this}
     */
    public FileCreateTransaction setExpirationTime(Instant expirationTime) {
        requireNotFrozen();
        Objects.requireNonNull(expirationTime);
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * @return                          list of keys
     */
    @Nullable
    public Collection<Key> getKeys() {
        return keys;
    }

    /**
     * <p>Set the keys which must sign any transactions modifying this file. Required.
     *
     * <p>All keys must sign to modify the file's contents or keys. No key is required
     * to sign for extending the expiration time (except the one for the operator account
     * paying for the transaction). Only one key must sign to delete the file, however.
     *
     * <p>To require more than one key to sign to delete a file, add them to a
     * {@link com.hedera.hashgraph.sdk.KeyList} and pass that here.
     *
     * <p>The network currently requires a file to have at least one key (or key list or threshold key)
     * but this requirement may be lifted in the future.
     *
     * @param keys The Key or Keys to be set
     * @return {@code this}
     */
    public FileCreateTransaction setKeys(Key... keys) {
        requireNotFrozen();
        this.keys = KeyList.of(keys);
        return this;
    }

    /**
     * @return                          byte string representation
     */
    public ByteString getContents() {
        return ByteString.copyFrom(contents);
    }

    /**
     * <p>Set the given byte array as the file's contents.
     *
     * <p>This may be omitted to create an empty file.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link PrecheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>In this case, you can use {@link FileAppendTransaction}, which automatically breaks the contents
     * into chunks for you, to append contents of arbitrary size.
     *
     * @param bytes the contents of the file.
     * @return {@code this}
     */
    public FileCreateTransaction setContents(byte[] bytes) {
        requireNotFrozen();
        Objects.requireNonNull(bytes);
        contents = bytes;
        return this;
    }

    /**
     * <p>Encode the given {@link String} as UTF-8 and set it as the file's contents.
     *
     * <p>This may be omitted to create an empty file.
     *
     * <p>The string can later be recovered from {@link FileContentsQuery#execute(Client)}
     * via {@link String#String(byte[], java.nio.charset.Charset)} using
     * {@link java.nio.charset.StandardCharsets#UTF_8}.
     *
     * <p>Note that total size for a given transaction is limited to 6KiB (as of March 2020) by the
     * network; if you exceed this you may receive a {@link PrecheckStatusException}
     * with {@link com.hedera.hashgraph.sdk.Status#TRANSACTION_OVERSIZE}.
     *
     * <p>In this case, you can use {@link FileAppendTransaction}, which automatically breaks the contents
     * into chunks for you, to append contents of arbitrary size.
     *
     * @param text the contents of the file.
     * @return {@code this}
     */
    public FileCreateTransaction setContents(String text) {
        requireNotFrozen();
        Objects.requireNonNull(text);
        contents = text.getBytes(StandardCharsets.UTF_8);
        return this;
    }

    /**
     * @return                          the file's memo field
     */
    public String getFileMemo() {
        return fileMemo;
    }

    /**
     * Assign a memo to the file (100 bytes max).
     *
     * @param memo                      memo string
     * @return {@code this}
     */
    public FileCreateTransaction setFileMemo(String memo) {
        requireNotFrozen();
        Objects.requireNonNull(memo);
        fileMemo = memo;
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return FileServiceGrpc.getCreateFileMethod();
    }

    @Override
    void validateChecksums(Client client) {
        // do nothing
    }

    /**
     * Initialize from transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getFileCreate();
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
        if (body.hasKeys()) {
            keys = KeyList.fromProtobuf(body.getKeys(), null);
        }
        contents = body.getContents().toByteArray();
        fileMemo = body.getMemo();
    }

    /**
     * Build the transaction body.
     *
     * @return {@code {@link com.hedera.hashgraph.sdk.proto.FileCreateTransactionBody builder}}
     */
    FileCreateTransactionBody.Builder build() {
        var builder = FileCreateTransactionBody.newBuilder();

        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        if (keys != null) {
            builder.setKeys(keys.toProtobuf());
        }
        builder.setContents(ByteString.copyFrom(contents));
        builder.setMemo(fileMemo);

        return builder;
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setFileCreate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setFileCreate(build());
    }
}
