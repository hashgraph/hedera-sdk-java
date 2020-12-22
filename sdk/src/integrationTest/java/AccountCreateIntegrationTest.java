import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountCreateIntegrationTest {
    @Test
    @DisplayName("Can create account with only initial balance and key")
    void canCreateAccountWithOnlyInitialBalanceAndKey() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var info = new AccountInfoQuery()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.accountId, accountId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), key.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(1));
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

    @Test
    @DisplayName("Can create account with no initial balance")
    void canCreateAccountWithNoInitialBalance() {
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

    @Test
    @DisplayName("Cannot create account with no key")
    void canNotCreateAccountWithNoKey() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new AccountCreateTransaction()
                    .setInitialBalance(new Hbar(1))
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.KEY_REQUIRED.toString()));

            client.close();
        });
    }
}
