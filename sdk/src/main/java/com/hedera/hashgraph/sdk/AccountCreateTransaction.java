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
import lombok.AccessLevel;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Create a new Hedera™ account.
 */
@Accessors(chain = true)
public final class AccountCreateTransaction extends Transaction<AccountCreateTransaction> {
    private static final Hbar DEFAULT_RECORD_THRESHOLD = Hbar.fromTinybars(Long.MAX_VALUE);

    @NonNull
    @Getter
    @Setter
    private AccountId proxyAccountId;

    @NonNull
    @Getter
    @Setter
    private Key key;

    @NonNull
    @Getter
    @Setter
    private String accountMemo = "";

    @NonNull
    @Getter
    @Setter
    private Hbar initialBalance = new Hbar(0);

    @Getter
    @Setter
    private boolean receiverSignatureRequired = false;

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

        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }

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
