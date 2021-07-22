import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ClientIntegrationTest {
    @Test
    @Disabled
    @DisplayName("setNetwork() functions correctly")
    void testReplaceNodes() {
        assertDoesNotThrow(() -> {
            @Var Map<String, AccountId> network = new HashMap<>();
            network.put("0.testnet.hedera.com:50211", new AccountId(3));
            network.put("1.testnet.hedera.com:50211", new AccountId(4));

            var testEnv = new IntegrationTestEnv(1);

            testEnv.client
                .setMaxQueryPayment(new Hbar(2))
                .setRequestTimeout(Duration.ofMinutes(2))
                .setNetwork(network);

            assertNotNull(testEnv.operatorId);

            // Execute two simple queries so we create a channel for each network node.
            new AccountBalanceQuery()
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            new AccountBalanceQuery()
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            network = new HashMap<>();
            network.put("1.testnet.hedera.com:50211", new AccountId(4));
            network.put("2.testnet.hedera.com:50211", new AccountId(5));

            testEnv.client.setNetwork(network);

            network = new HashMap<>();
            network.put("35.186.191.247:50211", new AccountId(4));
            network.put("35.192.2.25:50211", new AccountId(5));

            testEnv.client.setNetwork(network);

            testEnv.close();
        });
    }

    @Test
    void transactionIdNetworkIsVerified() {
        assertThrows(IllegalArgumentException.class, () -> {
            var client = Client.forPreviewnet();

            new AccountCreateTransaction()
                .setTransactionId(TransactionId.generate(AccountId.fromString("0.0.123-rmkyk")))
                .execute(client);
            client.close();
        });
    }

    @Test
    @DisplayName("`setMaxNodesPerTransaction()`")
    void testMaxNodesPerTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            testEnv.client.setMaxNodesPerTransaction(1);

            var transaction = new AccountDeleteTransaction()
                .setAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client);

            assertNotNull(transaction.getNodeAccountIds());
            assertEquals(1, transaction.getNodeAccountIds().size());

            testEnv.close();
        });
    }

    @Test
    void ping() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();
            var network = testEnv.client.getNetwork();
            var nodes = new ArrayList<>(network.values());

            assertFalse(nodes.isEmpty());

            var node = nodes.get(0);

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.ping(node);
            testEnv.client.close();
        });
    }

    @Test
    void pingAll() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.pingAll();

            var network = testEnv.client.getNetwork();
            var nodes = new ArrayList<>(network.values());

            assertFalse(nodes.isEmpty());

            var node = nodes.get(0);

            new AccountBalanceQuery()
                .setAccountId(node)
                .execute(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    void pingAllBadNetwork() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            testEnv.client.setMaxNodeAttempts(1);
            testEnv.client.setMaxNodesPerTransaction(2);

            var network = testEnv.client.getNetwork();

            var entries = new ArrayList<>(network.entrySet());
            assertTrue(entries.size() > 1);

            network.clear();
            network.put("in-process:name", entries.get(0).getValue());
            network.put(entries.get(1).getKey(), entries.get(1).getValue());

            testEnv.client.setNetwork(network);

            testEnv.client.pingAll();

            var nodes = new ArrayList<>(testEnv.client.getNetwork().values());
            assertFalse(nodes.isEmpty());

            var node = nodes.get(0);

            new AccountBalanceQuery()
                .setAccountId(node)
                .execute(testEnv.client);

            assertEquals(1, testEnv.client.getNetwork().values().size());

            testEnv.client.close();
        });
    }
}
