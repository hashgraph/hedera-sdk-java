import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.MaxAttemptsExceededException;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountBalanceIntegrationTest {
    @Test
    @DisplayName("can connect to previewnwet with TLS")
    void canConnectToPreviewnetWithTLS() throws Exception {
        var network = new HashMap<String, AccountId>();
        network.put("0.previewnet.hedera.com:50212", new AccountId(3));
        network.put("1.previewnet.hedera.com:50212", new AccountId(4));
        network.put("2.previewnet.hedera.com:50212", new AccountId(5));
        network.put("3.previewnet.hedera.com:50212", new AccountId(6));
        network.put("4.previewnet.hedera.com:50212", new AccountId(7));

        var client = Client.forNetwork(network);

        for (var entry : network.entrySet()) {
            new AccountBalanceQuery()
                .setNodeAccountIds(Collections.singletonList(entry.getValue()))
                .setAccountId(entry.getValue())
                .execute(client);
        }
        client.close();
    }

    @Test
    @DisplayName("can connect to testnet with TLS")
    void canConnectToTestnetWithTLS() throws Exception {
        var network = new HashMap<String, AccountId>();
        network.put("0.testnet.hedera.com:50212", new AccountId(3));
        network.put("1.testnet.hedera.com:50212", new AccountId(4));
        network.put("2.testnet.hedera.com:50212", new AccountId(5));
        network.put("3.testnet.hedera.com:50212", new AccountId(6));
        network.put("4.testnet.hedera.com:50212", new AccountId(7));

        var client = Client.forNetwork(network);

        for (var entry : network.entrySet()) {
            new AccountBalanceQuery()
                .setNodeAccountIds(Collections.singletonList(entry.getValue()))
                .setAccountId(entry.getValue())
                .execute(client);
        }

        client.close();
    }

    @Test
    @DisplayName("Can fetch balance for client operator")
    void canFetchBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertTrue(balance.hbars.toTinybars() > 0);

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch cost for the query")
    void getCostBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(new Hbar(1));

        var cost = balance.getCost(testEnv.client);

        var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

        assertTrue(accBalance.hbars.toTinybars() > 0);
        assertEquals(0, cost.toTinybars());

        testEnv.close();
    }

    @Test
    @DisplayName("Should ignore unresponsive nodes")
    void shouldIgnoreUnresponsiveNodes() throws Exception {
        var network = new HashMap<String, AccountId>();
        network.put("35.237.200.180:50211", new AccountId(3));
        network.put("35.242.233.154:50211", new AccountId(10));

        var client = Client.forNetwork(network);

        assertThrows(MaxAttemptsExceededException.class, () ->
            new AccountBalanceQuery()
                .setMaxAttempts(3)
                .setNodeAccountIds(Collections.singletonList(new AccountId(10)))
                .setAccountId(new AccountId(10))
                .execute(client)
        );

        new AccountBalanceQuery()
            .setMaxAttempts(3)
            .setNodeAccountIds(List.of(new AccountId(10), new AccountId(3)))
            .setAccountId(new AccountId(10))
            .execute(client);

        client.close();
    }

    @Test
    @DisplayName("Can fetch cost for the query, big max set")
    void getCostBigMaxBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(new Hbar(1000000));

        var cost = balance.getCost(testEnv.client);

        var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

        assertTrue(accBalance.hbars.toTinybars() > 0);

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch cost for the query, very small max set")
    void getCostSmallMaxBalanceForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .setMaxQueryPayment(Hbar.fromTinybars(1));

        var cost = balance.getCost(testEnv.client);

        var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

        assertTrue(accBalance.hbars.toTinybars() > 0);

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot fetch balance for invalid account ID")
    void canNotFetchBalanceForInvalidAccountId() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new AccountBalanceQuery()
                .setAccountId(AccountId.fromString("1.0.3"))
                .execute(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Can fetch token balances for client operator")
    void canFetchTokenBalancesForClientOperator() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        var response = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setInitialSupply(10000)
            .setDecimals(50)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .setSupplyKey(testEnv.operatorKey)
            .setFreezeDefault(false)
            .execute(testEnv.client);

        var tokenId = Objects.requireNonNull(response.getReceipt(testEnv.client).tokenId);

        var balance = new AccountBalanceQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertEquals(10000, balance.tokens.get(tokenId));
        assertEquals(50, balance.tokenDecimals.get(tokenId));

        testEnv.close(tokenId);
    }
}
