package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TransactionBuilder;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.CryptoCreateTransactionBody;
import com.hederahashgraph.api.proto.java.RealmID;
import com.hederahashgraph.api.proto.java.ShardID;
import com.hederahashgraph.api.proto.java.Transaction;
import com.hederahashgraph.api.proto.java.TransactionResponse;
import com.hederahashgraph.service.proto.java.CryptoServiceGrpc;

import java.time.Duration;

import javax.annotation.Nullable;

import io.grpc.MethodDescriptor;

// Corresponds to `CryptoCreateTransaction`
public final class AccountCreateTransaction extends TransactionBuilder<AccountCreateTransaction> {
    private final CryptoCreateTransactionBody.Builder builder = bodyBuilder.getCryptoCreateAccountBuilder()
        // Required fixed autorenew duration (roughly 1/4 year)
        .setAutoRenewPeriod(DurationHelper.durationFrom(Duration.ofMinutes(131_500)))
        // Default to maximum values for record thresholds. Without this records would be
        // auto-created whenever a send or receive transaction takes place for this new account. This should
        // be an explicit ask.
        .setSendRecordThreshold(Long.MAX_VALUE)
        .setReceiveRecordThreshold(Long.MAX_VALUE);

    public AccountCreateTransaction(@Nullable Client client) {
        super(client);
    }

    @Override
    public AccountCreateTransaction setTransactionId(TransactionId transactionId) {
        // Setting the transaction ID defaults the shard and realm IDs
        // If you truly want to create a _new_ realm, then you need to null the realm after setting
        // this

        if (!builder.hasShardID()) {
            setShardId(
                transactionId.getAccountId()
                    .getShardNum());
        }

        if (!builder.hasRealmID()) {
            setRealmId(
                transactionId.getAccountId()
                    .getRealmNum());
        }

        return super.setTransactionId(transactionId);
    }

    public AccountCreateTransaction setKey(PublicKey publicKey) {
        builder.setKey(publicKey.toKeyProto());
        return this;
    }

    public AccountCreateTransaction setInitialBalance(long initialBalance) {
        builder.setInitialBalance(initialBalance);
        return this;
    }

    public AccountCreateTransaction setProxyAccountId(AccountId accountId) {
        builder.setProxyAccountID(accountId.toProto());
        return this;
    }

    public AccountCreateTransaction setSendRecordThreshold(long sendRecordThreshold) {
        builder.setSendRecordThreshold(sendRecordThreshold);
        return this;
    }

    public AccountCreateTransaction setReceiveRecordThreshold(long receiveRecordThreshold) {
        builder.setReceiveRecordThreshold(receiveRecordThreshold);
        return this;
    }

    public AccountCreateTransaction setReceiverSignatureRequired(boolean receiverSignatureRequired) {
        builder.setReceiverSigRequired(receiverSignatureRequired);
        return this;
    }

    public AccountCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(DurationHelper.durationFrom(autoRenewPeriod));
        return this;
    }

    public AccountCreateTransaction setShardId(long shardId) {
        builder.setShardID(
            ShardID.newBuilder()
                .setShardNum(shardId));

        return this;
    }

    public AccountCreateTransaction setRealmId(long realmId) {
        builder.setRealmID(
            RealmID.newBuilder()
                .setRealmNum(realmId));

        return this;
    }

    public AccountCreateTransaction setNewRealmAdminKey(PublicKey publicKey) {
        builder.setNewRealmAdminKey(publicKey.toKeyProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.hasKey(), ".setKey() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }
}
