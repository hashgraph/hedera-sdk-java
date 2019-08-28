package com.hedera.hashgraph.sdk.examples.simple;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class GetAccountBalance {
    private GetAccountBalance() { }

    public static void main(String[] args) throws HederaException {
        AccountId operatorId = ExampleHelper.getOperatorId();
        Client client = ExampleHelper.createHederaClient();
        long balance = client.getAccountBalance(operatorId);

        System.out.println("balance = " + balance);
    }
}
