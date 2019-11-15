package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class TransferCrypto {
    private TransferCrypto() { }

    public static void main(String[] args) throws HederaException {
        Client client = ExampleHelper.createHederaClient();

        // Transfer X hbar from the operator of the client to the given account ID
        Transaction transaction = new CryptoTransferTransaction(client)
            .addSender(ExampleHelper.getOperatorId(), 10_000)
            .addRecipient(AccountId.fromString("0.0.3"), 10_000)
            .build();

        transaction.execute();
        // queryReceipt() waits for consensus
        transaction.queryReceipt();

        System.out.println("transferred 10_000 tinybar...");
    }
}
