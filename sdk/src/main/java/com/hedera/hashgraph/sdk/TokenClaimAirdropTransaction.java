// SPDX-License-Identifier: Apache-2.0
package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenClaimAirdropTransactionBody;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody.Builder;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import java.util.LinkedHashMap;

/**
 * Token claim airdrop<br/>
 * Complete one or more pending transfers on behalf of the
 * recipient(s) for an airdrop.
 *
 * The sender MUST have sufficient balance to fulfill the airdrop at the
 * time of claim. If the sender does not have sufficient balance, the
 * claim SHALL fail.<br/>
 * Each pending airdrop successfully claimed SHALL be removed from state and
 * SHALL NOT be available to claim again.<br/>
 * Each claim SHALL be represented in the transaction body and
 * SHALL NOT be restated in the record file.<br/>
 * All claims MUST succeed for this transaction to succeed.
 *
 * ### Block Stream Effects
 * The completed transfers SHALL be present in the transfer list.
 */
public class TokenClaimAirdropTransaction extends PendingAirdropLogic<TokenClaimAirdropTransaction> {

    /**
     * Constructor.
     */
    public TokenClaimAirdropTransaction() {
        defaultMaxTransactionFee = Hbar.from(1);
    }

    /**
     * Constructor.
     *
     * @param txs Compound list of transaction id's list of (AccountId, Transaction) records
     * @throws InvalidProtocolBufferException when there is an issue with the protobuf
     */
    TokenClaimAirdropTransaction(
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenClaimAirdropTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Build the transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.TokenClaimAirdropTransactionBody}
     */
    TokenClaimAirdropTransactionBody.Builder build() {
        var builder = TokenClaimAirdropTransactionBody.newBuilder();

        for (var pendingAirdropId : pendingAirdropIds) {
            builder.addPendingAirdrops(pendingAirdropId.toProtobuf());
        }

        return builder;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getTokenClaimAirdrop();
        for (var pendingAirdropId : body.getPendingAirdropsList()) {
            this.pendingAirdropIds.add(PendingAirdropId.fromProtobuf(pendingAirdropId));
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getClaimAirdropMethod();
    }

    @Override
    void onFreeze(Builder bodyBuilder) {
        bodyBuilder.setTokenClaimAirdrop(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setTokenClaimAirdrop(build());
    }
}
