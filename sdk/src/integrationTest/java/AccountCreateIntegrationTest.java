import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountCreateIntegrationTest {
    @Test
    @DisplayName("Can create account with only initial balance and key")
    void canCreateAccountWithOnlyInitialBalanceAndKey() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            assertEquals(info.accountId, accountId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), key.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(1));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            testEnv.cleanUpAndClose(accountId, key);
        });
    }

    @Test
    @DisplayName("Can create account with no initial balance")
    void canCreateAccountWithNoInitialBalance() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery()
                .setAccountId(accountId)
                .execute(testEnv.client);

            assertEquals(info.accountId, accountId);
            assertFalse(info.isDeleted);
            assertEquals(info.key.toString(), key.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(0));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            testEnv.cleanUpAndClose(accountId, key);
        });
    }

    @Test
    @DisplayName("Cannot create account with no key")
    void canNotCreateAccountWithNoKey() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new AccountCreateTransaction()
                    .setInitialBalance(new Hbar(1))
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.KEY_REQUIRED.toString()));

            testEnv.cleanUpAndClose();
        });
    }
}
