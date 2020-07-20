package com.hedera.hashgraph.sdk;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

public abstract class SingleTransactionBuilder<T extends SingleTransactionBuilder<T>> extends TransactionBuilder<TransactionId, Transaction, T> {
    @Override
    public final Transaction build(@Nullable Client client) {
        onBuild(bodyBuilder);

        if (client != null && bodyBuilder.getTransactionFee() == 0) {
            bodyBuilder.setTransactionFee(client.maxTransactionFee.toTinybars());
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

            inner.setBodyBytes(bodyBuilder.build().toByteString());

            return new Transaction(Collections.singletonList(inner));
        }

        if (bodyBuilder.hasTransactionID() && client != null) {
            // Pick N / 3 nodes from the client and build that many transactions
            // This is for fail-over so we can cycle through nodes

            var size = client.getNumberOfNodesForTransaction();
            var transactions =
                new ArrayList<com.hedera.hashgraph.sdk.proto.Transaction.Builder>(size);

            for (var i = 0; i < size; ++i) {
                transactions.add(inner.setBodyBytes(bodyBuilder
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
}
