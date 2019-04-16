package com.hedera.sdk.examples;

import com.hedera.sdk.HederaException;
import com.hedera.sdk.account.AccountBalanceQuery;

import java.util.Objects;

@SuppressWarnings("Duplicates")
public final class GetAccountBalance {
    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        var query = new AccountBalanceQuery(client).setAccount(Objects.requireNonNull(client.getOperatorId()));

        var balance = query.execute();

        System.out.println("balance = " + balance);
    }
}
