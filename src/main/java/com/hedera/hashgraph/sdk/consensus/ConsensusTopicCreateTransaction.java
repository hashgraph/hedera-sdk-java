package com.hedera.hashgraph.sdk.consensus;

import com.hedera.hashgraph.proto.ConsensusCreateTopicTransactionBody;
import com.hedera.hashgraph.proto.ConsensusServiceGrpc;
import com.hedera.hashgraph.proto.Transaction;
import com.hedera.hashgraph.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.HederaConstants;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;

import io.grpc.MethodDescriptor;

public class ConsensusTopicCreateTransaction extends TransactionBuilder<ConsensusTopicCreateTransaction> {
    private final ConsensusCreateTopicTransactionBody.Builder builder = bodyBuilder.getConsensusCreateTopicBuilder();

    public ConsensusTopicCreateTransaction() {
        super();

        // Set the required autorenew duration.
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(HederaConstants.DEFAULT_AUTORENEW_DURATION));
    }

    public ConsensusTopicCreateTransaction setTopicMemo(String topicMemo) {
        builder.setMemo(topicMemo);
        return this;
    }

    public ConsensusTopicCreateTransaction setAdminKey(PublicKey key) {
        builder.setAdminKey(key.toKeyProto());
        return this;
    }

    public ConsensusTopicCreateTransaction setSubmitKey(PublicKey key) {
        builder.setSubmitKey(key.toKeyProto());
        return this;
    }

    public ConsensusTopicCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(autoRenewPeriod));
        return this;
    }

    public ConsensusTopicCreateTransaction setAutoRenewAccountId(AccountId autoRenewAccountId) {
        builder.setAutoRenewAccount(autoRenewAccountId.toProto());
        return this;
    }

    @Override
    protected void doValidate() {
        // No local validation needed
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return ConsensusServiceGrpc.getCreateTopicMethod();
    }
}
