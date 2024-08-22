package com.hedera.hashgraph.sdk.examples;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Endpoint;
import com.hedera.hashgraph.sdk.IPv4Address;
import com.hedera.hashgraph.sdk.IPv4AddressPart;
import com.hedera.hashgraph.sdk.NodeCreateTransaction;
import com.hedera.hashgraph.sdk.NodeDeleteTransaction;
import com.hedera.hashgraph.sdk.NodeUpdateTransaction;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class DabExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(
        Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public static void main(String[] args)
        throws TimeoutException, PrecheckStatusException, ReceiptStatusException, InterruptedException {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Replace with your Hedera account ID
        AccountId accountId = AccountId.fromString("0.0.1999");
        String description = "Hedera™ cryptocurrency";
        String newDescription = "Hedera™ cryptocurrency - updated";

        // Set up IPv4 address
        IPv4Address ipv4Address = new IPv4Address();
        ipv4Address.setHost(new IPv4AddressPart());
        ipv4Address.setNetwork(new IPv4AddressPart());
        Endpoint gossipEndpoint = new Endpoint();
        gossipEndpoint.setAddress(ipv4Address);

        // Set up service endpoint
        Endpoint serviceEndpoint = new Endpoint();
        serviceEndpoint.setAddress(ipv4Address);

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
        } catch (Exception e){
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
        } catch (Exception e){
            System.out.println(e);
        }

        var nodeDeleteTransaction = new NodeDeleteTransaction()
            .setNodeId(123);

        try {
            nodeDeleteTransaction.execute(client).getReceipt(client);
        } catch (Exception e){
            System.out.println(e);
        }
    }
}
