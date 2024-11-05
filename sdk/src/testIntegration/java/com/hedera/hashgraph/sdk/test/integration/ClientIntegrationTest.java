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

import com.hedera.hashgraph.sdk.*;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ClientIntegrationTest {

    @Test
    @DisplayName("fails when all the manually set nodes are not matching the address book")
    void failsWhenNoNodesAreMatching() throws Exception {
        var client = Client.forTestnet()
            .setTransportSecurity(true);

        var nodes = new ArrayList<AccountId>();
        nodes.add(new AccountId(1000));
        nodes.add(new AccountId(1001));
        assertThatExceptionOfType(IllegalStateException.class).isThrownBy(() -> new AccountBalanceQuery()
            .setNodeAccountIds(nodes)
            .setAccountId(new AccountId(7))
            .execute(client)).withMessageContaining("All node account IDs did not map to valid nodes in the client's network");
        client.close();
    }

    @Test
    @DisplayName("can skip invalid nodes")
    void canSkipNodes() throws Exception {
        var client = Client.forTestnet()
            .setTransportSecurity(true);

        var nodes = new ArrayList<>(client.getNetwork().values().stream().toList());
        nodes.add(new AccountId(1000));
        new AccountBalanceQuery()
            .setNodeAccountIds(nodes)
            .setAccountId(new AccountId(7))
            .execute(client);

        client.close();
    }

    @Test
    @DisplayName("setNetwork() functions correctly")
    void testReplaceNodes() throws Exception {
        Map<String, AccountId> network = new HashMap<>();
        network.put("0.testnet.hedera.com:50211", new AccountId(3));
        network.put("1.testnet.hedera.com:50211", new AccountId(4));

        try (var testEnv = new IntegrationTestEnv(1)) {

            testEnv.client
                .setMaxQueryPayment(new Hbar(2))
                .setRequestTimeout(Duration.ofMinutes(2))
                .setNetwork(network);

            assertThat(testEnv.operatorId).isNotNull();

            // Execute two simple queries so we create a channel for each network node.
            new AccountBalanceQuery()
                .setAccountId(new AccountId(3))
                .execute(testEnv.client);

            new AccountBalanceQuery()
                .setAccountId(new AccountId(3))
                .execute(testEnv.client);

            network = new HashMap<>();
            network.put("1.testnet.hedera.com:50211", new AccountId(4));
            network.put("2.testnet.hedera.com:50211", new AccountId(5));

            testEnv.client.setNetwork(network);

            network = new HashMap<>();
            network.put("35.186.191.247:50211", new AccountId(4));
            network.put("35.192.2.25:50211", new AccountId(5));

            testEnv.client.setNetwork(network);

        }
    }

    @Test
    void transactionIdNetworkIsVerified() {
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> {
            var client = Client.forPreviewnet();
            client.setAutoValidateChecksums(true);

            new AccountCreateTransaction()
                .setTransactionId(TransactionId.generate(AccountId.fromString("0.0.123-esxsf")))
                .execute(client);
            client.close();
        });
    }

    @Test
    @DisplayName("`setMaxNodesPerTransaction()`")
    void testMaxNodesPerTransaction() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {

            testEnv.client.setMaxNodesPerTransaction(1);

            var transaction = new AccountDeleteTransaction()
                .setAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client);

            assertThat(transaction.getNodeAccountIds()).isNotNull();
            assertThat(transaction.getNodeAccountIds().size()).isEqualTo(1);

        }
    }

    @Test
    void ping() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {
            var network = testEnv.client.getNetwork();
            var nodes = new ArrayList<>(network.values());

            assertThat(nodes.isEmpty()).isFalse();

            var node = nodes.get(0);

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.ping(node);
        }
    }

    @Test
    void pingAll() throws Exception {
        try (var testEnv = new IntegrationTestEnv()) {

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.pingAll();

            var network = testEnv.client.getNetwork();
            var nodes = new ArrayList<>(network.values());

            assertThat(nodes.isEmpty()).isFalse();

            var node = nodes.get(0);

            new AccountBalanceQuery()
                .setAccountId(node)
                .execute(testEnv.client);

        }
    }

    @Test
    void pingAllBadNetwork() throws Exception {
        try (var testEnv = new IntegrationTestEnv(3)) {

            // Skip if using local node.
            // Note: this check should be removed once the local node is supporting multiple nodes.
            testEnv.assumeNotLocalNode();

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.setMaxAttempts(1);
            testEnv.client.setMaxNodesPerTransaction(2);

            var network = testEnv.client.getNetwork();

            var entries = new ArrayList<>(network.entrySet());
            assertThat(entries.size()).isGreaterThan(1);

            network.clear();
            network.put("in-process:name", entries.get(0).getValue());
            network.put(entries.get(1).getKey(), entries.get(1).getValue());

            testEnv.client.setNetwork(network);

            assertThatExceptionOfType(MaxAttemptsExceededException.class).isThrownBy(() -> {
                testEnv.client.pingAll();
            }).withMessageContaining("exceeded maximum attempts");

            var nodes = new ArrayList<>(testEnv.client.getNetwork().values());
            assertThat(nodes.isEmpty()).isFalse();

            var node = nodes.get(0);

            new AccountBalanceQuery()
                .setAccountId(node)
                .execute(testEnv.client);

            assertThat(testEnv.client.getNetwork().values().size()).isEqualTo(1);
        }

    }

    @Test
    void pingAsync() throws Exception {
        try (var testEnv = new IntegrationTestEnv(1)) {
            var network = testEnv.client.getNetwork();
            var nodes = new ArrayList<>(network.values());

            assertThat(nodes.isEmpty()).isFalse();

            var node = nodes.get(0);

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.pingAsync(node).get();
        }
    }

    @Test
    void pingAllAsync() throws Exception {
        try (var testEnv = new IntegrationTestEnv()) {

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.pingAllAsync().get();

            var network = testEnv.client.getNetwork();
            var nodes = new ArrayList<>(network.values());

            assertThat(nodes.isEmpty()).isFalse();

            var node = nodes.get(0);

            new AccountBalanceQuery()
                .setAccountId(node)
                .execute(testEnv.client);

        }
    }
}
