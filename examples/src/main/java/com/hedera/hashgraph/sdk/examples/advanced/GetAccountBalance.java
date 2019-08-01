package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class GetAccountBalance {
    private GetAccountBalance() { }

    public static void main(String[] args) throws HederaException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();

        var query = new AccountBalanceQuery(client).setAccountId(operatorId);

        var cost = query.requestCost();

        System.out.println("balance query cost: " + cost);

        var balance = query.execute();

        System.out.println("balance = " + balance);
    }
}
