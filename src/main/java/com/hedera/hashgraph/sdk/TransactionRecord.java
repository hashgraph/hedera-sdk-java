package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.proto.AccountAmountOrBuilder;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public final class TransactionRecord {
    private final com.hedera.hashgraph.sdk.proto.TransactionRecord inner;

    TransactionRecord(com.hedera.hashgraph.sdk.proto.TransactionRecord inner) {
        this.inner = inner;
    }

    public TransactionId getTransactionId() {
        return new TransactionId(inner.getTransactionIDOrBuilder());
    }

    public long getTransactionFee() {
        return inner.getTransactionFee();
    }

    public TransactionReceipt getReceipt() {
        return new TransactionReceipt(inner.getReceipt());
    }

    @Nullable
    public byte[] getTransactionHash() {
        var hash = inner.getTransactionHash();
        // proto specifies hash is not provided if the transaction failed due to a duplicate ID
        return !hash.isEmpty() ? hash.toByteArray() : null;
    }

    @Nullable
    public Instant getConsensusTimestamp() {
        return inner.hasConsensusTimestamp() ? TimestampHelper.timestampTo(inner.getConsensusTimestamp()) : null;
    }

    @Nullable
    public String getMemo() {
        var memo = inner.getMemo();
        return !memo.isEmpty() ? memo : null;
    }

    @Nullable
    public FunctionResult getCallResult() {
        return inner.hasContractCallResult() ? new FunctionResult(inner.getContractCallResultOrBuilder()) : null;
    }

    @Nullable
    public FunctionResult getCreateResult() {
        return inner.hasContractCreateResult() ? new FunctionResult(inner.getContractCreateResultOrBuilder()) : null;
    }

    @Nullable
    public List<Transfer> getTransfers() {
        return inner.hasTransferList()
            ? inner.getTransferList()
                .getAccountAmountsList()
                .stream()
                .map(Transfer::new)
                .collect(Collectors.toList())
            : null;
    }

    public final static class Transfer {
        public final AccountId account;
        public final long amount;

        private Transfer(AccountAmountOrBuilder accountAmount) {
            account = new AccountId(accountAmount.getAccountIDOrBuilder());
            amount = accountAmount.getAmount();
        }
    }
}
