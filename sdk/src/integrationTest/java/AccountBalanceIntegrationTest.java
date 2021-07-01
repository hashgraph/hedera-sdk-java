import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountBalanceIntegrationTest {
    @Test
    @DisplayName("Can fetch balance for client operator")
    void canFetchBalanceForClientOperator() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            @Var var balance = new AccountBalanceQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            assertTrue(balance.hbars.toTinybars() > 0);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can fetch cost for the query")
    void getCostBalanceForClientOperator() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            @Var var balance = new AccountBalanceQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.operatorId)
                .setMaxQueryPayment(new Hbar(1));

            var cost = balance.getCost(testEnv.client);

            var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

            assertTrue(accBalance.hbars.toTinybars() > 0);
            assertEquals(cost.toTinybars(), 0);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can fetch cost for the query, big max set")
    void getCostBigMaxBalanceForClientOperator() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            @Var var balance = new AccountBalanceQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.operatorId)
                .setMaxQueryPayment(new Hbar(1000000));

            var cost = balance.getCost(testEnv.client);

            var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

            assertTrue(accBalance.hbars.toTinybars() > 0);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can fetch cost for the query, very small max set")
    void getCostSmallMaxBalanceForClientOperator() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            @Var var balance = new AccountBalanceQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.operatorId)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = balance.getCost(testEnv.client);

            var accBalance = balance.setQueryPayment(cost).execute(testEnv.client);

            assertTrue(accBalance.hbars.toTinybars() > 0);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot fetch balance for invalid account ID")
    void canNotFetchBalanceForInvalidAccountId() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new AccountBalanceQuery()
                    .setNodeAccountIds(testEnv.nodeAccountIds)
                    .setAccountId(AccountId.fromString("1.0.3"))
                    .execute(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can fetch token balances for client operator")
    void canFetchTokenBalancesForClientOperator() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new TokenCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
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

            @Var var balance = new AccountBalanceQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            assertEquals(balance.tokens.get(tokenId), 10000);
            assertEquals(balance.tokenDecimals.get(tokenId), 50);

            testEnv.client.close();
        });
    }
}
