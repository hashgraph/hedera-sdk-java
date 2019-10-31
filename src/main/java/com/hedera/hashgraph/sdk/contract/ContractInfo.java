package com.hedera.hashgraph.sdk.contract;

import com.hedera.hashgraph.sdk.DurationHelper;
import com.hedera.hashgraph.sdk.TimestampHelper;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hederahashgraph.api.proto.java.ContractGetInfoResponse;
import com.hederahashgraph.api.proto.java.Response;

import java.time.Duration;
import java.time.Instant;

import javax.annotation.Nullable;

public final class ContractInfo {
    private final ContractGetInfoResponse.ContractInfo inner;

    ContractInfo(Response response) {
        if (!response.hasContractGetInfo()) {
            throw new IllegalArgumentException("response was not `contractGetInfo`");
        }

        inner = response.getContractGetInfo()
            .getContractInfo();
    }

    public ContractId getContractId() {
        return new ContractId(inner.getContractIDOrBuilder());
    }

    public AccountId getAccountId() {
        return new AccountId(inner.getAccountIDOrBuilder());
    }

    public String getContractAccountId() {
        return inner.getContractAccountID();
    }

    @Nullable
    public PublicKey getAdminKey() {
        return inner.hasAdminKey() ? PublicKey.fromProtoKey(inner.getAdminKey()) : null;
    }

    public Instant getExpirationTime() {
        return TimestampHelper.timestampTo(inner.getExpirationTime());
    }

    public Duration getAutoRenewPeriod() {
        return DurationHelper.durationTo(inner.getAutoRenewPeriod());
    }

    public long getStorage() {
        return inner.getStorage();
    }
}
