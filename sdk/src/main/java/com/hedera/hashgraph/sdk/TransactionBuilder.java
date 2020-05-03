package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TransactionBody;
import java8.util.concurrent.CompletableFuture;
import org.threeten.bp.Duration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public abstract class TransactionBuilder<T extends TransactionBuilder<T>>
    extends Executable<TransactionId> {
    // Default auto renew duration for accounts, contracts, topics, and files (entities)
    static final Duration DEFAULT_AUTO_RENEW_PERIOD = Duration.ofDays(90);

    // Default transaction duration
    private static final Duration DEFAULT_TRANSACTION_VALID_DURATION = Duration.ofSeconds(120);

    private final TransactionBody.Builder bodyBuilder;

    private final com.hedera.hashgraph.sdk.proto.Transaction.Builder builder;

    TransactionBuilder() {
        builder = com.hedera.hashgraph.sdk.proto.Transaction.newBuilder();
        bodyBuilder = TransactionBody.newBuilder();

        setTransactionValidDuration(DEFAULT_TRANSACTION_VALID_DURATION);
    }

    /**
     * Set the ID for this transaction.
     *
     * <p>The transaction ID includes the operator's account ( the account paying the transaction
     * fee). If two transactions have the same transaction ID, they won't both have an effect. One
     * will complete normally and the other will fail with a duplicate transaction status.
     *
     * <p>Normally, you should not use this method. Just before a transaction is executed, a
     * transaction ID will be generated from the operator on the client.
     *
     * @return {@code this}
     * @see TransactionId
     */
    public final T setTransactionId(TransactionId transactionId) {
        bodyBuilder.setTransactionID(transactionId.toProtobuf());

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Set the account ID of the node that this transaction will be submitted to.
     *
     * <p>Providing an explicit node account ID interferes with client-side load balancing of the
     * network. By default, the SDK will pre-generate a transaction for 1/3 of the nodes on the
     * network. If a node is down, busy, or otherwise reports a fatal error, the SDK will try again
     * with a different node.
     *
     * @return {@code this}
     */
    public final T setNodeAccountId(AccountId nodeAccountId) {
        bodyBuilder.setNodeAccountID(nodeAccountId.toProtobuf());

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Sets the duration that this transaction is valid for.
     *
     * <p>This is defaulted by the SDK to 120 seconds (or two minutes).
     *
     * @return {@code this}
     */
    public final T setTransactionValidDuration(Duration validDuration) {
        bodyBuilder.setTransactionValidDuration(DurationConverter.toProtobuf(validDuration));

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Set the maximum transaction fee the operator (paying account) is willing to pay.
     *
     * @param maxTransactionFee the maximum transaction fee, in tinybars.
     * @return {@code this}
     */
    public final T setMaxTransactionFee(Hbar maxTransactionFee) {
        bodyBuilder.setTransactionFee(maxTransactionFee.asTinybar());

        // noinspection unchecked
        return (T) this;
    }

    /**
     * Set a note or description that should be recorded in the transaction record (maximum length
     * of 100 characters).
     *
     * @param memo any notes or descriptions for this transaction.
     * @return {@code this}
     */
    public final T setTransactionMemo(String memo) {
        bodyBuilder.setMemo(memo);

        // noinspection unchecked
        return (T) this;
    }

    public final Transaction build(@Nullable Client client) {
        onBuild(bodyBuilder);

        if (client != null && bodyBuilder.getTransactionFee() == 0) {
            bodyBuilder.setTransactionFee(client.maxTransactionFee.asTinybar());
        }

        if (!bodyBuilder.hasTransactionID() && client != null) {
            var operator = client.getOperator();
            if (operator != null) {
                // Set a default transaction ID, generated from the operator account ID
                setTransactionId(TransactionId.generate(operator.accountId));

            } else {
                // no client means there must be an explicitly set node ID and transaction ID
                throw new IllegalStateException(
                    "`client` must have an `operator` or `transactionId` must be set");

            }
        }

        if (bodyBuilder.hasTransactionID() && bodyBuilder.hasNodeAccountID()) {
            // Emplace the body into the transaction wrapper
            // This wrapper object contains the bytes for the body and signatures of the body

            builder.setBodyBytes(bodyBuilder.build().toByteString());

            return new Transaction(Collections.singletonList(builder));
        }

        if (bodyBuilder.hasTransactionID() && client != null) {
            // Pick N / 3 nodes from the client and build that many transactions
            // This is for fail-over so we can cycle through nodes

            var size = client.getNumberOfNodesForTransaction();
            var transactions =
                new ArrayList<com.hedera.hashgraph.sdk.proto.Transaction.Builder>(size);

            for (var i = 0; i < size; ++i) {
                transactions.add(builder.setBodyBytes(bodyBuilder
                    .setNodeAccountID(client.getNextNodeId().toProtobuf())
                    .build()
                    .toByteString()
                ).clone());
            }

            return new Transaction(transactions);
        }

        throw new IllegalStateException(
            "`client` must not be NULL or both a `nodeAccountId` and `transactionId` must be set");
    }

    @Override
    public final CompletableFuture<TransactionId> executeAsync(Client client) {
        return build(client).executeAsync(client);
    }

    /**
     * Called in {@link #build} just before the transaction body is built. The intent is for the
     * derived class to assign their data variant to the transaction body.
     */
    abstract void onBuild(TransactionBody.Builder bodyBuilder);
}
