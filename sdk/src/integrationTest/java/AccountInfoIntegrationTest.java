import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountInfoIntegrationTest {
    @Test
    @DisplayName("Can query account info for client operator")
    void canQueryAccountInfoForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var info = new AccountInfoQuery()
                .setAccountId(operatorId)
                .execute(client);

            assertEquals(info.accountId, operatorId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), operatorKey.toString());
            assertTrue(info.balance.toTinybars() > 0);
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            client.close();
        });
    }

    @Test
    @DisplayName("Can query account info for account with only key set")
    void canQueryAccountInfoForAccountWithOnlyKeySet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var info = new AccountInfoQuery()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.accountId, accountId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), key.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(0));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
