import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountBalanceIntegrationTest {
    @Test
    @DisplayName("Can fetch balance for client operator")
    void canFetchBalanceForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            @Var var balance = new AccountBalanceQuery()
                .setAccountId(operatorId)
                .execute(client);

            assertTrue(balance.hbars.toTinybars() > 0);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot fetch balance for invalid account ID")
    void canNotFetchBalanceForInvalidAccountId() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new AccountBalanceQuery()
                    .setAccountId(AccountId.fromString("1.0.3"))
                    .execute(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Can fetch token balances for client operator")
    void canFetchTokenBalancesForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setInitialSupply(10000)
                .setTreasuryAccountId(operatorId)
                .setAdminKey(operatorKey)
                .setSupplyKey(operatorKey)
                .setFreezeDefault(false)
                .execute(client);

            var tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);

            @Var var balance = new AccountBalanceQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(operatorId)
                .execute(client);

            assertTrue(balance.token.get(tokenId) > 0);

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
