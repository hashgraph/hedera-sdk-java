package com.hedera.sdk.examples.advanced;

import com.hedera.sdk.*;
import com.hedera.sdk.account.AccountUpdateTransaction;
import com.hedera.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.sdk.examples.ExampleHelper;

public final class UpdateAccountPublicKey {
    public static void main(String[] args) throws HederaException {
        var client = ExampleHelper.createHederaClient();

        // First, we create a new account so we don't affect our account

        var originalKey = Ed25519PrivateKey.generate();
        var accountId = client.createAccount(originalKey.getPublicKey(), 0);

        // Next, we update the key

        var newKey = Ed25519PrivateKey.generate();

        System.out.println(" :: update public key of account " + accountId);
        System.out.println("set key = " + newKey.getPublicKey());

        new AccountUpdateTransaction(client).setAccountForUpdate(accountId)
            .setKey(newKey.getPublicKey())
            // Sign with the previous key and the new key
            .sign(originalKey)
            .sign(newKey)
            .executeForReceipt();

        // Now we fetch the account information to check if the key was changed

        System.out.println(" :: getAccount and check our current key");

        var info = client.getAccount(accountId);

        System.out.println("key = " + info.getKey());
    }
}
