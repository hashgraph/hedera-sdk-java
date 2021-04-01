import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountUpdateIntegrationTest {
    @Test
    @DisplayName("Can update account with a new key")
    void canUpdateAccountWithNewKey() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key1 = PrivateKey.generate();
            var key2 = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key1)
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            @Var var info = new AccountInfoQuery()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.accountId, accountId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), key1.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(0));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            new AccountUpdateTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setKey(key2.getPublicKey())
                .freezeWith(client)
                .sign(key1)
                .sign(key2)
                .execute(client)
                .getReceipt(client);

            info = new AccountInfoQuery()
                .setAccountId(accountId)
                .execute(client);

            assertEquals(info.accountId, accountId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), key2.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(0));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key2)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot update account when account ID is not set")
    void cannotUpdateAccountWhenAccountIdIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new AccountUpdateTransaction()
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_ACCOUNT_ID.toString()));

            client.close();
        });
    }
}
