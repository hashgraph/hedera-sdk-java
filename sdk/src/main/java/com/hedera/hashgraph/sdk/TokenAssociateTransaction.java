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
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenAssociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

/**
 * The transaction that will associate accounts to a token id.
 */
public class TokenAssociateTransaction extends Transaction<TokenAssociateTransaction> {
    @Nullable
    private AccountId accountId;
    private List<TokenId> tokenIds = new ArrayList<>();

    /**
     * Constructor.
     */
    public TokenAssociateTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenAssociateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenAssociateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
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
     * Assign the account id.
     *
     * @param accountId                 the account id
     * @return {@code this}
     */
    public TokenAssociateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    /**
     * Extract the list of token id's.
     *
     * @return                          the list of token id's
     */
    public List<TokenId> getTokenIds() {
        return new ArrayList<>(tokenIds);
    }

    /**
     * Assign a new list of token id's.
     *
     * @param tokens                    the list of token id's
     * @return {@code this}
     */
    public TokenAssociateTransaction setTokenIds(List<TokenId> tokens) {
        Objects.requireNonNull(tokens);
        requireNotFrozen();
        this.tokenIds = new ArrayList<>(tokens);
        return this;
    }

    /**
     * Build the transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.TokenAssociateTransactionBody}
     */
    TokenAssociateTransactionBody.Builder build() {
        var builder = TokenAssociateTransactionBody.newBuilder();
        if (accountId != null) {
            builder.setAccount(accountId.toProtobuf());
        }

        for (var token : tokenIds) {
            if (token != null) {
                builder.addTokens(token.toProtobuf());
            }
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenAssociate();
        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }

        for (var token : body.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        Objects.requireNonNull(client);
        if (accountId != null) {
            accountId.validateChecksum(client);
        }

        for (var token : tokenIds) {
            if (token != null) {
                token.validateChecksum(client);
            }
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getAssociateTokensMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAssociate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenAssociate(build());
    }
}
