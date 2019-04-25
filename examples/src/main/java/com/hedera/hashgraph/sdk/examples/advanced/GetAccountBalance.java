package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class GetAccountBalance {
    public static void main(String[] args) throws HederaException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();

        var query = new AccountBalanceQuery(client).setAccountId(operatorId);

        var balance = query.execute();

        System.out.println("balance = " + balance);
    }
}
