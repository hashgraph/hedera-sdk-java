import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
}
