package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class TransferCrypto {
    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        // Transfer X hbar from the operator of the client to the given account ID
        client.transferCryptoTo(AccountId.fromString("0.0.3"), 10_000);

        System.out.println("transferred 10_000 tinybar...");
    }
}
