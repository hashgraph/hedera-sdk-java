package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.file.FileContentsQuery;
import com.hedera.hashgraph.sdk.file.FileId;

public final class GetQueryCost {
    private GetQueryCost() { }

    public static void main(String[] args) throws HederaException {

        // Build the Hedera client using ExampleHelper class
        var client = ExampleHelper.createHederaClient();

        System.out.println("balance before: " + client.getAccountBalance(ExampleHelper.getOperatorId()));

        // Get file contents
        var cost = new FileContentsQuery(client)
            // fileNum 102 is the fee schedule
            .setFileId(new FileId(0, 0, 102))
            .addAutoPayment(9)
            .requestCost();

        System.out.println("balance after: " + client.getAccountBalance(ExampleHelper.getOperatorId()));

        // Prints query results to console
        System.out.println("File content query cost: " + cost);
    }
}
