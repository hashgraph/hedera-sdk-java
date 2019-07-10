package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.AccountAddClaimTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.util.Random;

public final class AddClaim {
    private AddClaim() { }

    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        var userId = ExampleHelper.getOperatorId();
        var userPubKey = ExampleHelper.getOperatorKey().getPublicKey();

        // the byte claim should be a 48 byte long hash
        // using random bytes for sake of example
        var bytesClaim = new byte[48];
        new Random().nextBytes(bytesClaim);

        var tx = new AccountAddClaimTransaction(client)
            .addKey(userPubKey)
            .setAccountId(userId)
            .setHash(bytesClaim);

        System.out.println("Adding claim to " + userId.toString());

        var receipt = tx.executeForReceipt();

        System.out.println("Succesfully added claim to " + receipt.getAccountId().toString());
    }
}
