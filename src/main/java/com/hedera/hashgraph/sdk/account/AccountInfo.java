package com.hedera.hashgraph.sdk.account;

import com.hedera.hashgraph.proto.CryptoGetInfoResponse;
import com.hedera.hashgraph.proto.Response;
import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.crypto.PublicKey;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * @deprecated this constructor is being hidden in 1.0.
     */
    @Deprecated
    public AccountInfo(CryptoGetInfoResponse.AccountInfoOrBuilder info) {
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

    /**
     * @deprecated is now a public field.
     * @return
     */
    @Deprecated
    public AccountId getAccountId() {
        return accountId;
    }

    public String getContractAccountId() {
        return contractAccountId;
    }

    @Deprecated
    public boolean isDeleted() {
        return isDeleted;
    }

    @Deprecated
    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    @Deprecated
    public long getProxyReceived() {
        return proxyReceived;
    }

    @Deprecated
    public PublicKey getKey() {
        return key;
    }

    @Deprecated
    public long getBalance() {
        return balance;
    }

    @Deprecated
    public long getGenerateSendRecordThreshold() {
        return generateSendRecordThreshold;
    }

    @Deprecated
    public long getGenerateReceiveRecordThreshold() {
        return generateReceiveRecordThreshold;
    }

    @Deprecated
    public boolean isReceiverSignatureRequired() {
        return isReceiverSignatureRequired;
    }

    @Deprecated
    public Instant getExpirationTime() {
        return expirationTime;
    }

    @Deprecated
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * @deprecated for removal
     */
    @Deprecated
    public List<Claim> getClaims() {
        // claims were never implemented so an empty list is fine
        return new ArrayList<>();
    }

    static AccountInfo fromResponse(Response response) {
        if (!response.hasCryptoGetInfo()) {
            throw new IllegalArgumentException("query response was not `CryptoGetInfoResponse`");
        }

        CryptoGetInfoResponse infoResponse = response.getCryptoGetInfo();

        return new AccountInfo(infoResponse.getAccountInfo());
    }
}
