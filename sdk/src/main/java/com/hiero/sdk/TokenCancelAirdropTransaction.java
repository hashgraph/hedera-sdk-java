/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
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
package com.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hiero.sdk.proto.SchedulableTransactionBody;
import com.hiero.sdk.proto.TokenCancelAirdropTransactionBody;
import com.hiero.sdk.proto.TokenServiceGrpc;
import com.hiero.sdk.proto.TransactionBody.Builder;
import com.hiero.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;

public class TokenCancelAirdropTransaction extends PendingAirdropLogic<TokenCancelAirdropTransaction> {

    /**
     * Constructor.
     */
    public TokenCancelAirdropTransaction() {
        defaultMaxTransactionFee = Hbar.from(1);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    TokenCancelAirdropTransaction(
        LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hiero.sdk.proto.Transaction>> txs)
        throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenCancelAirdropTransaction(com.hiero.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }


    /**
     * Build the transaction body.
     *
     * @return {@link com.hiero.sdk.proto.TokenCancelAirdropTransactionBody}
     */
    TokenCancelAirdropTransactionBody.Builder build() {
        var builder = TokenCancelAirdropTransactionBody.newBuilder();

        for (var pendingAirdropId : pendingAirdropIds) {
            builder.addPendingAirdrops(pendingAirdropId.toProtobuf());
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenCancelAirdrop();
        for (var pendingAirdropId : body.getPendingAirdropsList()) {
            this.pendingAirdropIds.add(PendingAirdropId.fromProtobuf(pendingAirdropId));
        }
    }

    @Override
    MethodDescriptor<com.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getCancelAirdropMethod();
    }

    @Override
    void onFreeze(Builder bodyBuilder) {
        bodyBuilder.setTokenCancelAirdrop(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenCancelAirdrop(build());
    }
}
