package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

public final class GetAccountBalance {
    private GetAccountBalance() { }

    public static void main(String[] args) throws HederaException {
        AccountId operatorId = ExampleHelper.getOperatorId();
        Client client = ExampleHelper.createHederaClient();

        AccountBalanceQuery query = new AccountBalanceQuery(client).setAccountId(operatorId);

        Long balance = query.execute();

        System.out.println("balance = " + balance);
    }
}
