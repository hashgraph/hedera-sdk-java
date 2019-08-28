package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.account.AccountAddClaimTransaction;
import com.hedera.hashgraph.sdk.account.AccountId;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PublicKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;

import java.util.Random;

public final class AddClaim {
    private AddClaim() { }

    public static void main(String[] args) throws HederaException {
        Client client = ExampleHelper.createHederaClient();

        AccountId userId = ExampleHelper.getOperatorId();
        Ed25519PublicKey userPubKey = ExampleHelper.getOperatorKey().getPublicKey();

        // the byte claim should be a 48 byte long hash
        // using random bytes for sake of example
        byte[] bytesClaim = new byte[48];
        new Random().nextBytes(bytesClaim);

        AccountAddClaimTransaction tx = new AccountAddClaimTransaction(client)
            .addKey(userPubKey)
            .setAccountId(userId)
            .setHash(bytesClaim);

        System.out.println("Adding claim to " + userId.toString());

        TransactionReceipt receipt = tx.executeForReceipt();

        System.out.println("Succesfully added claim to " + receipt.getAccountId().toString());
    }
}
