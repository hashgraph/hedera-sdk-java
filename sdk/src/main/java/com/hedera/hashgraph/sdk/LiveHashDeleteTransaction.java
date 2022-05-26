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
import com.hedera.hashgraph.sdk.proto.CryptoDeleteLiveHashTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * At consensus, deletes a livehash associated to the given account. The transaction must be signed
 * by either the key of the owning account, or at least one of the keys associated to the livehash.
 */
public final class LiveHashDeleteTransaction extends Transaction<LiveHashDeleteTransaction> {
    @Nullable
    private AccountId accountId = null;
    private byte[] hash = {};

    /**
     * Constructor.
     */
    public LiveHashDeleteTransaction() {
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    LiveHashDeleteTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Extract the account id.
     *
     * @return                          the account id
     */
    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    /**
     * The account owning the livehash
     *
     * @param accountId The AccountId to be set
     * @return {@code this}
     */
    public LiveHashDeleteTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the hash.
     *
     * @return                          the hash
     */
    public ByteString getHash() {
        return ByteString.copyFrom(hash);
    }

    /**
     * The SHA-384 livehash to delete from the account
     *
     * @param hash The array of bytes to be set as hash
     * @return {@code this}
     */
    public LiveHashDeleteTransaction setHash(byte[] hash) {
        requireNotFrozen();
        Objects.requireNonNull(hash);
        this.hash = hash;
        return this;
    }

    /**
     * The SHA-384 livehash to delete from the account
     *
     * @param hash The array of bytes to be set as hash
     * @return {@code this}
     */
    public LiveHashDeleteTransaction setHash(ByteString hash) {
        Objects.requireNonNull(hash);
        return setHash(hash.toByteArray());
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoDeleteLiveHash();
        if (body.hasAccountOfLiveHash()) {
            accountId = AccountId.fromProtobuf(body.getAccountOfLiveHash());
        }
        hash = body.getLiveHashToDelete().toByteArray();
    }

    /**
     * Build the correct transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.CryptoAddLiveHashTransactionBody}
     */
    CryptoDeleteLiveHashTransactionBody.Builder build() {
        var builder = CryptoDeleteLiveHashTransactionBody.newBuilder();
        if (accountId != null) {
            builder.setAccountOfLiveHash(accountId.toProtobuf());
        }
        builder.setLiveHashToDelete(ByteString.copyFrom(hash));

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (accountId != null) {
            accountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getDeleteLiveHashMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoDeleteLiveHash(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        throw new UnsupportedOperationException("Cannot schedule LiveHashDeleteTransaction");
    }
}
