package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.TransactionBuilder;
import com.hedera.sdk.TransactionId;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.*;
import com.hedera.sdk.proto.Transaction;
import io.grpc.MethodDescriptor;
import java.time.Duration;

// Corresponds to `CryptoCreateTransaction`
public final class AccountCreateTransaction extends TransactionBuilder<AccountCreateTransaction> {

    private final CryptoCreateTransactionBody.Builder builder;

    public AccountCreateTransaction() {
        // Recommendation from Hedera
        setAutoRenewPeriod(Duration.ofDays(30));

        builder = inner.getBodyBuilder().getCryptoCreateAccountBuilder();

        // Default to maximum values for record thresholds. Without this records would be
        // auto-created
        // whenever a send or receive transaction takes place for this new account. This should
        // be an explicit ask.
        builder.setSendRecordThreshold(Long.MAX_VALUE).setReceiveRecordThreshold(Long.MAX_VALUE);
    }

    @Override
    public AccountCreateTransaction setTransactionId(TransactionId transactionId) {
        // Setting the transaction ID defaults the shard and realm IDs
        // If you truly want to create a _new_ realm, then you need to null the realm after setting
        // this

        if (!builder.hasShardID()) {
            setShardId(transactionId.getAccountId().getShardNum());
        }

        if (!builder.hasRealmID()) {
            setRealmId(transactionId.getAccountId().getRealmNum());
        }

        return super.setTransactionId(transactionId);
    }

    public AccountCreateTransaction setKey(Key publicKey) {
        builder.setKey(publicKey.toKeyProto());
        return this;
    }

    public AccountCreateTransaction setInitialBalance(long initialBalance) {
        builder.setInitialBalance(initialBalance);
        return this;
    }

    public AccountCreateTransaction setproxyAccountId(AccountId accountId) {
        builder.setProxyAccountID(accountId.toProto());
        return this;
    }

    public AccountCreateTransaction setProxyFraction(int proxyFraction) {
        builder.setProxyFraction(proxyFraction);
        return this;
    }

    public AccountCreateTransaction setMaxReceiveProxyFraction(int maxReceiveProxyFraction) {
        builder.setMaxReceiveProxyFraction(maxReceiveProxyFraction);
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

    public AccountCreateTransaction setReceiverSignatureRequired(
            boolean receiverSignatureRequired) {
        builder.setReceiverSigRequired(receiverSignatureRequired);
        return this;
    }

    public AccountCreateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        builder.setAutoRenewPeriod(
                com.hedera.sdk.proto.Duration.newBuilder()
                        .setSeconds(autoRenewPeriod.getSeconds())
                        .setNanos(autoRenewPeriod.getNano()));

        return this;
    }

    public AccountCreateTransaction setShardId(long shardId) {
        builder.setShardID(ShardID.newBuilder().setShardNum(shardId));

        return this;
    }

    public AccountCreateTransaction setRealmId(long realmId) {
        builder.setRealmID(RealmID.newBuilder().setRealmNum(realmId));

        return this;
    }

    public AccountCreateTransaction setNewRealmAdminKey(Key publicKey) {
        builder.setNewRealmAdminKey(publicKey.toKeyProto());
        return this;
    }

    @Override
    protected void doValidate() {
        require(builder.getKeyOrBuilder(), ".setKey() required");
    }

    @Override
    protected MethodDescriptor<Transaction, TransactionResponse> getMethod() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }
}
