/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TokenId;
import com.hedera.hashgraph.sdk.TransferTransaction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Assumptions;

public class IntegrationTestEnv implements AutoCloseable {
    static final String LOCAL_CONSENSUS_NODE_ENDPOINT = "127.0.0.1:50211";
    static final String LOCAL_MIRROR_NODE_GRPC_ENDPOINT = "127.0.0.1:5600";
    static final AccountId LOCAL_CONSENSUS_NODE_ACCOUNT_ID = new AccountId(3);
    private final Client originalClient;
    public Client client;
    public PublicKey operatorKey;
    public AccountId operatorId;
    public boolean isLocalNode = false;

    public IntegrationTestEnv() throws Exception {
        this(0);
    }

    @SuppressWarnings("EmptyCatch")
    public IntegrationTestEnv(int maxNodesPerTransaction) throws Exception {
        client = createTestEnvClient();

        if (maxNodesPerTransaction == 0) {
            maxNodesPerTransaction = client.getNetwork().size();
        }

        client.setMaxNodesPerTransaction(maxNodesPerTransaction);
        originalClient = client;

        try {
            var operatorPrivateKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));
            operatorKey = operatorPrivateKey.getPublicKey();

            client.setOperator(operatorId, operatorPrivateKey);
        } catch (RuntimeException ignored) {
        }

        operatorKey = client.getOperatorPublicKey();
        operatorId = client.getOperatorAccountId();

        assertThat(client.getOperatorAccountId()).isNotNull();
        assertThat(client.getOperatorPublicKey()).isNotNull();

        if (client.getNetwork().size() > 0 && (client.getNetwork().containsKey(LOCAL_CONSENSUS_NODE_ENDPOINT))) {
            isLocalNode = true;
        }

        var nodeGetter = new TestEnvNodeGetter(client);
        var network = new HashMap<String, AccountId>();

        var nodeCount = Math.min(client.getNetwork().size(), maxNodesPerTransaction);
        for (@Var int i = 0; i < nodeCount; i++) {
            nodeGetter.nextNode(network);
        }
        client.setNetwork(network);
    }

    @SuppressWarnings("EmptyCatch")
    private static Client createTestEnvClient() throws Exception {
        if (System.getProperty("HEDERA_NETWORK").equals("previewnet")) {
            return Client.forPreviewnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("testnet")) {
            return Client.forTestnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("localhost")) {
            var network = new HashMap<String, AccountId>();
            network.put(LOCAL_CONSENSUS_NODE_ENDPOINT, LOCAL_CONSENSUS_NODE_ACCOUNT_ID);

            return Client
                .forNetwork(network)
                .setMirrorNetwork(List.of(LOCAL_MIRROR_NODE_GRPC_ENDPOINT));
        } else if (!System.getProperty("CONFIG_FILE").equals("")) {
            try {
                return Client.fromConfigFile(System.getProperty("CONFIG_FILE"));
            } catch (Exception configFileException) {
                configFileException.printStackTrace();
            }
        }
        throw new IllegalStateException("Failed to construct client for IntegrationTestEnv");
    }

    public IntegrationTestEnv useThrowawayAccount(Hbar initialBalance) throws Exception {
        var key = PrivateKey.generateED25519();
        operatorKey = key.getPublicKey();
        operatorId = new AccountCreateTransaction()
            .setInitialBalance(initialBalance)
            .setKey(key)
            .execute(client)
            .getReceipt(client)
            .accountId;

        client = Client.forNetwork(originalClient.getNetwork());
        client.setMirrorNetwork(originalClient.getMirrorNetwork());
        client.setOperator(Objects.requireNonNull(operatorId), key);
        client.setLedgerId(originalClient.getLedgerId());
        return this;
    }

    public IntegrationTestEnv useThrowawayAccount() throws Exception {
        return useThrowawayAccount(new Hbar(50));
    }

    // Note: this is a temporary workaround.
    // The assumption should be removed once the local node is supporting multiple nodes.
    public void assumeNotLocalNode() throws Exception {
        // first clean up the current IntegrationTestEnv...
        if (isLocalNode) {
            close();
        }

        // then skip the current test
        Assumptions.assumeFalse(isLocalNode);
    }

    public void close(
        @Nullable TokenId newTokenId,
        @Nullable AccountId newAccountId,
        @Nullable PrivateKey newAccountKey
    ) throws Exception {
        if (newAccountId != null) {
            wipeAccountHbars(newAccountId, newAccountKey);
        }

        if (!operatorId.equals(originalClient.getOperatorAccountId())) {
            var hbarsBalance = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(originalClient)
                .hbars;
            new TransferTransaction()
                .addHbarTransfer(operatorId, hbarsBalance.negated())
                .addHbarTransfer(Objects.requireNonNull(originalClient.getOperatorAccountId()), hbarsBalance)
                .freezeWith(originalClient)
                .signWithOperator(client)
                .execute(originalClient);
            client.close();
        }

        originalClient.close();
    }

    public void wipeAccountHbars(AccountId newAccountId, PrivateKey newAccountKey) throws Exception {
        var hbarsBalance = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(originalClient)
            .hbars;
        new TransferTransaction()
            .addHbarTransfer(newAccountId, hbarsBalance.negated())
            .addHbarTransfer(Objects.requireNonNull(originalClient.getOperatorAccountId()), hbarsBalance)
            .freezeWith(originalClient)
            .sign(Objects.requireNonNull(newAccountKey))
            .execute(originalClient);
    }

    @Override
    public void close() throws Exception {
        close(null, null, null);
    }

    public void close(AccountId newAccountId, PrivateKey newAccountKey) throws Exception {
        close(null, newAccountId, newAccountKey);
    }

    public void close(TokenId newTokenId) throws Exception {
        close(newTokenId, null, null);
    }

    private static class TestEnvNodeGetter {
        private final Client client;
        private final List<Map.Entry<String, AccountId>> nodes;
        @Var
        private int index = 0;

        public TestEnvNodeGetter(Client client) {
            this.client = client;
            nodes = new ArrayList<>(client.getNetwork().entrySet());
            Collections.shuffle(nodes);
        }

        public void nextNode(Map<String, AccountId> outMap) throws Exception {
            if (nodes.isEmpty()) {
                throw new IllegalStateException(
                    "IntegrationTestEnv needs another node, but there aren't enough nodes in client network");
            }
            for (; index < nodes.size(); index++) {
                var node = nodes.get(index);
                try {
                    new TransferTransaction()
                        .setNodeAccountIds(Collections.singletonList(node.getValue()))
                        .setMaxAttempts(1)
                        .addHbarTransfer(client.getOperatorAccountId(), Hbar.fromTinybars(1).negated())
                        .addHbarTransfer(AccountId.fromString("0.0.3"), Hbar.fromTinybars(1))
                        .execute(client)
                        .getReceipt(client);
                    nodes.remove(index);
                    outMap.put(node.getKey(), node.getValue());
                    return;
                } catch (Throwable err) {
                    System.err.println(err);
                }
            }
            throw new Exception("Failed to find working node in " + nodes + " for IntegrationTestEnv");
        }
    }
}
