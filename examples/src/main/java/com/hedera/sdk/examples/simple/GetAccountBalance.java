package com.hedera.sdk.examples.simple;

import com.hedera.sdk.HederaException;
import com.hedera.sdk.examples.ExampleHelper;

public final class GetAccountBalance {
    public static void main(String[] args) throws HederaException {
        var operatorId = ExampleHelper.getOperatorId();
        var client = ExampleHelper.createHederaClient();
        var balance = client.getAccountBalance(operatorId);

        System.out.println("balance = " + balance);
    }
}
