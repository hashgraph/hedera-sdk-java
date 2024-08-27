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
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenAirdropTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;

/**
 * Token Airdrop
 * An "airdrop" is a distribution of tokens from a funding account
 * to one or more recipient accounts, ideally with no action required
 * by the recipient account(s).
 */
public class TokenAirdropTransaction extends AbstractTokenTransferTransaction<TokenAirdropTransaction> {
    /**
     * Constructor.
     */
    public TokenAirdropTransaction() {
        super();
        defaultMaxTransactionFee = new Hbar(1);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction)
     *            records
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    TokenAirdropTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenAirdropTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Build the transaction body.
     *
     * @return {@link
     *         com.hedera.hashgraph.sdk.proto.TokenAirdropTransactionBody}
     */
    TokenAirdropTransactionBody.Builder build() {
        var transfers = sortTransfersAndBuild();
        var builder = TokenAirdropTransactionBody.newBuilder();

        for (var transfer : transfers) {
            builder.addTokenTransfers(transfer.toProtobuf());
        }

        return builder;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getAirdropTokensMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenAirdrop(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenAirdrop(build());
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenAirdrop();

        for (var tokenTransferList : body.getTokenTransfersList()) {
            var token = TokenId.fromProtobuf(tokenTransferList.getToken());

            for (var transfer : tokenTransferList.getTransfersList()) {
                tokenTransfers.add(new TokenTransfer(
                    token,
                    AccountId.fromProtobuf(transfer.getAccountID()),
                    transfer.getAmount(),
                    tokenTransferList.hasExpectedDecimals() ? tokenTransferList.getExpectedDecimals().getValue() : null,
                    transfer.getIsApproval()
                ));
            }

            for (var transfer : tokenTransferList.getNftTransfersList()) {
                nftTransfers.add(new TokenNftTransfer(
                    token,
                    AccountId.fromProtobuf(transfer.getSenderAccountID()),
                    AccountId.fromProtobuf(transfer.getReceiverAccountID()),
                    transfer.getSerialNumber(),
                    transfer.getIsApproval()
                ));
            }
        }
    }
}
