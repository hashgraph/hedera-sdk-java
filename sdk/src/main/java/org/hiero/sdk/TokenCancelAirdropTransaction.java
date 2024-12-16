// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import org.hiero.sdk.proto.SchedulableTransactionBody;
import org.hiero.sdk.proto.TokenCancelAirdropTransactionBody;
import org.hiero.sdk.proto.TokenServiceGrpc;
import org.hiero.sdk.proto.TransactionBody.Builder;
import org.hiero.sdk.proto.TransactionResponse;
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
            LinkedHashMap<TransactionId, LinkedHashMap<AccountId, org.hiero.sdk.proto.Transaction>> txs)
            throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    TokenCancelAirdropTransaction(org.hiero.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Build the transaction body.
     *
     * @return {@link org.hiero.sdk.proto.TokenCancelAirdropTransactionBody}
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
    MethodDescriptor<org.hiero.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
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
