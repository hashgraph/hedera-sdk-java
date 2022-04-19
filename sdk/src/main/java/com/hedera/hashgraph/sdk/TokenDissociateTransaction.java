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
import com.hedera.hashgraph.sdk.proto.TokenDissociateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

public class TokenDissociateTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenDissociateTransaction> {
    @Nullable
    private AccountId accountId = null;
    private List<TokenId> tokenIds = new ArrayList<>();

    public TokenDissociateTransaction() {
        defaultMaxTransactionFee = new Hbar(5);
    }

    TokenDissociateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    TokenDissociateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    @Nullable
    public AccountId getAccountId() {
        return accountId;
    }

    public TokenDissociateTransaction setAccountId(AccountId accountId) {
        Objects.requireNonNull(accountId);
        requireNotFrozen();
        this.accountId = accountId;
        return this;
    }

    public List<TokenId> getTokenIds() {
        return new ArrayList<>(tokenIds);
    }

    public TokenDissociateTransaction setTokenIds(List<TokenId> tokens) {
        requireNotFrozen();
        this.tokenIds = new ArrayList<>(tokens);
        return this;
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenDissociate();
        if (body.hasAccount()) {
            accountId = AccountId.fromProtobuf(body.getAccount());
        }

        for (var token : body.getTokensList()) {
            tokenIds.add(TokenId.fromProtobuf(token));
        }
    }

    TokenDissociateTransactionBody.Builder build() {
        var builder = TokenDissociateTransactionBody.newBuilder();
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

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
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
        return TokenServiceGrpc.getDissociateTokensMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenDissociate(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenDissociate(build());
    }
}
