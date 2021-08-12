package com.hedera.hashgraph.sdk;

import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.CryptoCreateTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.CryptoServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.threeten.bp.Duration;
import lombok.experimental.Accessors;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.LinkedHashMap;

/**
 * Create a new Hedera™ account.
 */
@Accessors(chain = true)
public final class AccountCreateTransaction extends Transaction<AccountCreateTransaction> {

    /**
     * The ID of the account to which this account is proxy staked.
     */
    @NonNull
    @Getter
    @Setter
    private AccountId proxyAccountId;

    /**
     * The key for this account.
     *
     * <p>The key that must sign each transfer out of the account. If receiverSignatureRequired is
     * true, then it must also sign any transfer into the account.
     */
    @NonNull
    @Getter
    @Setter
    private Key key;

    @NonNull
    @Getter
    @Setter
    private String accountMemo = "";

    /**
     * Set the initial amount to transfer into this account.
     */
    @NonNull
    @Getter
    @Setter
    private Hbar initialBalance = new Hbar(0);

    /**
     * If set to true will require this account to sign any transfer of hbars to this account.
     *
     * <p>All transfers of hbars from this account must always be signed. This property only affects
     * transfers to this account.
     */
    @Getter
    @Setter
    private boolean receiverSignatureRequired = false;

    /**
     * The auto renew period for this account.
     *
     * <p>A Hedera™ account is charged to extend its expiration date every renew period. If it
     * doesn't have enough balance, it extends as long as possible. If the balance is zero when it
     * expires, then the account is deleted.
     *
     * <p>This is defaulted to 3 months by the SDK.
     */
    @NonNull
    @Getter
    @Setter
    private Duration autoRenewPeriod = DEFAULT_AUTO_RENEW_PERIOD;

    public AccountCreateTransaction() {
    }

    AccountCreateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }

    AccountCreateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    CryptoCreateTransactionBody.Builder build() {
        var builder = CryptoCreateTransactionBody.newBuilder();

        if (key != null) {
            builder.setKey(key.toProtobufKey());
        }

        if (initialBalance != null) {
            builder.setInitialBalance(initialBalance.toTinybars());
        }

        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if(key != null) {
            builder.setKey(key.toProtobufKey());
        }
        builder.setInitialBalance(initialBalance.toTinybars());
        builder.setReceiverSigRequired(receiverSignatureRequired);
        builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        builder.setMemo(accountMemo);

        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }

    void initFromTransactionBody() {
        var body = sourceTransactionBody.getCryptoCreateAccount();

        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        
        if(body.hasKey()) {
            key = Key.fromProtobufKey(body.getKey());
        }

        if(body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }

        initialBalance = Hbar.fromTinybars(body.getInitialBalance());
        accountMemo = body.getMemo();
        receiverSignatureRequired = body.getReceiverSigRequired();
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return CryptoServiceGrpc.getCreateAccountMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setCryptoCreateAccount(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setCryptoCreateAccount(build());
    }
}
