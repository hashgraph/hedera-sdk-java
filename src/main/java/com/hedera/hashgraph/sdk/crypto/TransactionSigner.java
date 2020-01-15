package com.hedera.hashgraph.sdk.crypto;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountId;

/**
 * Functional interface for lambdas which can sign and return a transaction.
 *
 * @see Client#setOperatorWith(AccountId, PublicKey, TransactionSigner)
 * @see Transaction#signWith(PublicKey, TransactionSigner)
 */
@FunctionalInterface
public interface TransactionSigner {
    /**
     * Sign the transaction. The implementation may block, e.g. while waiting for user
     * confirmation.
     *
     * The kind of signature this callback returns is interpreted based on the kind of
     * {@link PublicKey} passed alongside this signer.
     *
     * @param transactionBytes the bytes of the transaction body.
     * @return the signature.
     */
    byte[] signTransaction(byte[] transactionBytes);
}
