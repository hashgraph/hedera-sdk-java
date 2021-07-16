import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete account")
    void canDeleteAccount() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var info = new AccountInfoQuery()
                .setAccountId(accountId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
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
    @DisplayName("Cannot delete invalid account ID")
    void cannotCreateAccountWithNoKey() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new AccountDeleteTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setTransferAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot delete account that has not signed transaction")
    void cannotDeleteAccountThatHasNotSignedTransaction() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new AccountDeleteTransaction()
                    .setAccountId(accountId)
                    .setTransferAccountId(testEnv.operatorId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            testEnv.cleanUpAndClose(accountId, key);
        });
    }
}
