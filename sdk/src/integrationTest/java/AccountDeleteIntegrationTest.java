import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete account")
    void canDeleteAccount() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

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

        testEnv.close(accountId, key);
    }

    @Test
    @DisplayName("Cannot delete invalid account ID")
    void cannotCreateAccountWithNoKey() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new AccountDeleteTransaction()
                .setTransferAccountId(testEnv.operatorId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot delete account that has not signed transaction")
    void cannotDeleteAccountThatHasNotSignedTransaction() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
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

        testEnv.close(accountId, key);
    }
}
