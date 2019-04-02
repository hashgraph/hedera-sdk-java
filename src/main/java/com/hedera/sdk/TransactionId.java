package com.hedera.sdk;

import com.hedera.sdk.proto.Timestamp;
import com.hedera.sdk.proto.TransactionID;
import java.time.Clock;
import java.time.Instant;

// TODO: TransactionId.toString
// TODO: TransactionId.fromString

public final class TransactionId {
    final TransactionID.Builder inner;

    /**
     * Generates a new transaction ID for the given `accountId`.
     *
     * <p>Note that transaction IDs are made up of the current time and the account that is
     * primarily signing the transaction. This account will also be the account that is charged for
     * any transaction fees.
     */
    public TransactionId(AccountId accountId) {
        this(
                accountId, Clock.systemUTC()
                    .instant()
        );
    }

    private TransactionId(AccountId accountId, Instant transactionValidStart) {
        inner = TransactionID.newBuilder()
            .setAccountID(accountId.inner)
            .setTransactionValidStart(
                Timestamp.newBuilder()
                    .setSeconds(transactionValidStart.getEpochSecond())
                    .setNanos(transactionValidStart.getNano())
            );
    }

    public AccountId getAccountId() {
        return AccountId.fromProto(inner.getAccountIDBuilder());
    }

    public TransactionID toProto() {
        return inner.build();
    }
}
