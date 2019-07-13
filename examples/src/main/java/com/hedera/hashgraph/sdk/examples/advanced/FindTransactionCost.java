package com.hedera.hashgraph.sdk.examples.advanced;

import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.HederaException;
import com.hedera.hashgraph.sdk.account.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.crypto.Key;
import com.hedera.hashgraph.sdk.crypto.ed25519.Ed25519PrivateKey;
import com.hedera.hashgraph.sdk.examples.ExampleHelper;
import com.hedera.hashgraph.sdk.proto.ResponseCodeEnum;

public final class FindTransactionCost {
    private FindTransactionCost() { }

    public static void main(String[] args) {
        // Generate a Ed25519 private, public key pair
        var key = Ed25519PrivateKey.generate().getPublicKey();
        var client = ExampleHelper.createHederaClient();

        var upperBound = 100000000000L;
        var lowerBound = 100L;
        var attempt = 0;

        while (!Thread.interrupted() && upperBound != lowerBound) {
            attempt += 1;
            final var tryCost = (upperBound + lowerBound) / 2;

            System.out.print("\rlower bound = "
                + lowerBound + "; upper bound = " + upperBound + "; attempted cost = " + tryCost);

            try {
                tryTransaction(client, key, tryCost);
            } catch (HederaException e) {
                if (e.responseCode == ResponseCodeEnum.INSUFFICIENT_TX_FEE) {
                    lowerBound = tryCost + 1;
                    continue;
                }

                throw new RuntimeException(e);
            }

            upperBound = tryCost;
        }

        System.out.println("\r\n found cost = " + lowerBound + " in " + attempt + " attempts");
    }

    private static void tryTransaction(Client client, Key key, long fee) throws HederaException {
        final var tx = new CryptoTransferTransaction(client)
            .addSender(ExampleHelper.getOperatorId(), 0)
            /*.addSender(ExampleHelper.getOperatorId(), 100_000_000)
            .addRecipient(ExampleHelper.getNodeId(), 50_000_000)
            .addRecipient(new AccountId(0, 0, 4), 25_000_000)
            .addRecipient(new AccountId(0, 0, 5), 25_000_000)*/
            .setTransactionFee(fee)
            .build();

        tx.executeForReceipt();
    }
}
