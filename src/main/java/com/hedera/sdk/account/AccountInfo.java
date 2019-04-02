package com.hedera.sdk.account;

import com.hedera.sdk.AccountId;
import com.hedera.sdk.Claim;
import com.hedera.sdk.DurationHelper;
import com.hedera.sdk.TimestampHelper;
import com.hedera.sdk.crypto.Key;
import com.hedera.sdk.proto.CryptoGetInfoResponse;
import com.hedera.sdk.proto.Response;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public class AccountInfo {
    private final CryptoGetInfoResponse.AccountInfo inner;

    public AccountId getAccountId() {
        return AccountId.fromProto(inner.getAccountIDOrBuilder());
    }

    public String getContractAccountId() {
        return inner.getContractAccountID();
    }

    public boolean isDeleted() {
        return inner.getDeleted();
    }

    @Nullable
    public AccountId getProxyAccountId() {
        return inner.hasProxyAccountID() ? AccountId.fromProto(inner.getProxyAccountIDOrBuilder()) : null;
    }

    public int getProxyFraction() {
        return inner.getProxyFraction();
    }

    public long getProxyReceived() {
        return inner.getProxyReceived();
    }

    public Key getKey() {
        return Key.fromProtoKey(inner.getKey());
    }

    public long getBalance() {
        return inner.getBalance();
    }

    public long getGenerateSendRecordThreshold() {
        return inner.getGenerateSendRecordThreshold();
    }

    public long getGenerateReceiveRecordThreshold() {
        return inner.getGenerateReceiveRecordThreshold();
    }

    public boolean isReceiverSignatureRequired() {
        return inner.getReceiverSigRequired();
    }

    public Instant getExpirationTime() {
        return TimestampHelper.timestampTo(inner.getExpirationTime());
    }

    public Duration getAutoRenewPeriod() {
        return DurationHelper.durationTo(inner.getAutoRenewPeriod());
    }

    public List<Claim> getClaims() {
        return inner.getClaimsList()
            .stream()
            .map(Claim::fromProto)
            .collect(Collectors.toList());
    }

    AccountInfo(Response response) {
        if (!response.hasCryptoGetInfo()) {
            throw new IllegalArgumentException("query response was not `CryptoGetInfoResponse`");
        }

        var infoResponse = response.getCryptoGetInfo();
        inner = infoResponse.getAccountInfo();

        if (!inner.hasKey()) {
            throw new IllegalArgumentException("query response missing key");
        }
    }
}
