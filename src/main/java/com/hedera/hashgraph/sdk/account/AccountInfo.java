package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

public final class AccountInfo {
    public final AccountId accountId;
    public final String contractAccountId;

    public final boolean isDeleted;

    @Nullable
    public final AccountId proxyAccountId;
    public final long proxyReceived;

    public final PublicKey key;

    public final long balance;

    public final long generateSendRecordThreshold;
    public final long generateReceiveRecordThreshold;

    public final boolean isReceiverSignatureRequired;

    public final Instant expirationTime;

    public final Duration autoRenewPeriod;

    AccountInfo(CryptoGetInfoResponse.AccountInfoOrBuilder info) {
        if (!info.hasKey()) {
            throw new IllegalArgumentException("query response missing key");
        }

        accountId = new AccountId(info.getAccountIDOrBuilder());
        contractAccountId = info.getContractAccountID();
        isDeleted = info.getDeleted();
        proxyAccountId = info.hasProxyAccountID() ? new AccountId(info.getProxyAccountIDOrBuilder()) : null;
        proxyReceived = info.getProxyReceived();
        key = PublicKey.fromProtoKey(info.getKeyOrBuilder());
        balance = info.getBalance();
        generateSendRecordThreshold = info.getGenerateSendRecordThreshold();
        generateReceiveRecordThreshold = info.getGenerateReceiveRecordThreshold();
        isReceiverSignatureRequired = info.getReceiverSigRequired();
        expirationTime = TimestampHelper.timestampTo(info.getExpirationTime());
        autoRenewPeriod = DurationHelper.durationTo(info.getAutoRenewPeriod());
    }

    static AccountInfo fromResponse(Response response) {
        if (!response.hasCryptoGetInfo()) {
            throw new IllegalArgumentException("query response was not `CryptoGetInfoResponse`");
        }

        CryptoGetInfoResponse infoResponse = response.getCryptoGetInfo();

        return new AccountInfo(infoResponse.getAccountInfo());
    }
}
