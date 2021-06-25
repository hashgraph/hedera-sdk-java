import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.NetworkName;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClientIntegrationTest {
    @Test
    @DisplayName("setNetwork() functions correctly")
    void testReplaceNodes() {
        assertDoesNotThrow(() -> {
            @Var Map<String, AccountId> network = new HashMap<>();
            network.put("0.testnet.hedera.com:50211", new AccountId(3));
            network.put("1.testnet.hedera.com:50211", new AccountId(4));

            var testEnv = new IntegrationTestEnv();

            testEnv.client
                .setMaxQueryPayment(new Hbar(2))
                .setRequestTimeout(Duration.ofMinutes(2));

            var operatorId = testEnv.client.getOperatorAccountId();
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

            testEnv.client.close();
        });
    }

    @Test
    void transactionIdNetworkIsVerified() {
        assertThrows(IllegalArgumentException.class, () -> {
            var client = Client.forPreviewnet();

            new AccountCreateTransaction()
                .setTransactionId(TransactionId.generate(AccountId.fromString("0.0.123-rmkyk")))
                .execute(client);
        });
    }

    @Test
    @DisplayName("`setMaxNodesPerTransaction()`")
    void testMaxNodesPerTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            testEnv.client.setMaxNodesPerTransaction(1);

            var transaction = new AccountDeleteTransaction()
                .setAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client);

            assertNotNull(transaction.getNodeAccountIds());
            assertEquals(1, transaction.getNodeAccountIds().size());

            testEnv.client.close();
        });
    }
}
