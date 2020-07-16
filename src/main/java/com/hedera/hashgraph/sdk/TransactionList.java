package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.crypto.PrivateKey;
import com.hedera.hashgraph.sdk.crypto.PublicKey;
import com.hedera.hashgraph.sdk.crypto.TransactionSigner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TransactionList {
    private final Collection<Transaction> transactions;

    @Internal
    public TransactionList(Collection<Transaction> transactions) {
        this.transactions = transactions;
    }

    public TransactionList sign(PrivateKey<? extends PublicKey> privateKey) {
        return signWith(privateKey.publicKey, privateKey::sign);
    }

    public TransactionList signWith(PublicKey publicKey, TransactionSigner signer) {
        for (Transaction transaction : transactions) {
            transaction.signWith(publicKey, signer);
        }

        return this;
    }

    public final List<TransactionId> execute(Client client) throws HederaStatusException, HederaNetworkException, LocalValidationException {
        ArrayList<TransactionId> ids = new ArrayList<>();

        for (Transaction transaction : transactions) {
            ids.add(transaction.execute(client));
        }

        return ids;
    }
}
