// SPDX-License-Identifier: Apache-2.0
package org.hiero.sdk.java.examples;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import org.hiero.sdk.java.AccountId;
import org.hiero.sdk.java.Client;
import org.hiero.sdk.java.Endpoint;
import org.hiero.sdk.java.NodeCreateTransaction;
import org.hiero.sdk.java.NodeDeleteTransaction;
import org.hiero.sdk.java.NodeUpdateTransaction;
import org.hiero.sdk.java.PrecheckStatusException;
import org.hiero.sdk.java.PrivateKey;
import org.hiero.sdk.java.ReceiptStatusException;

/**
 * hip-869
 */
public class DynamicAddressBookExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID =
            AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY =
            PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args)
            throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        AccountId accountId = AccountId.fromString("0.0.1999");
        String description = "Hedera™ cryptocurrency";
        String newDescription = "Hedera™ cryptocurrency - updated";

        // Set up IPv4 address
        Endpoint gossipEndpoint = new Endpoint();
        gossipEndpoint.setAddress(new byte[] {0x00, 0x01, 0x02, 0x03});

        // Set up service endpoint
        Endpoint serviceEndpoint = new Endpoint();
        serviceEndpoint.setAddress(new byte[] {0x00, 0x01, 0x02, 0x03});

        // Generate admin key
        PrivateKey adminKey = PrivateKey.generateED25519();

        // Create node create transaction
        NodeCreateTransaction nodeCreateTransaction = new NodeCreateTransaction()
                .setAccountId(accountId)
                .setDescription(description)
                .setGossipCaCertificate("gossipCaCertificate".getBytes())
                .setServiceEndpoints(Collections.singletonList(serviceEndpoint))
                .setGossipEndpoints(Collections.singletonList(gossipEndpoint))
                .setAdminKey(adminKey.getPublicKey());

        try {
            nodeCreateTransaction.execute(client).getReceipt(client);
        } catch (Exception e) {
            System.out.println(e);
        }

        var nodeUpdateTransaction = new NodeUpdateTransaction()
                .setNodeId(123)
                .setAccountId(accountId)
                .setDescription(newDescription)
                .setGossipCaCertificate("gossipCaCertificate".getBytes())
                .setServiceEndpoints(Collections.singletonList(serviceEndpoint))
                .setGossipEndpoints(Collections.singletonList(gossipEndpoint))
                .setAdminKey(adminKey.getPublicKey());

        try {
            nodeUpdateTransaction.execute(client).getReceipt(client);
        } catch (Exception e) {
            System.out.println(e);
        }

        var nodeDeleteTransaction = new NodeDeleteTransaction().setNodeId(123);

        try {
            nodeDeleteTransaction.execute(client).getReceipt(client);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
