import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete account")
    void canDeleteAccount() {
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
    @DisplayName("Cannot delete invalid account ID")
    void canNotCreateAccountWithNoKey() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new AccountDeleteTransaction()
                    .setTransferAccountId(operatorId)
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.ACCOUNT_ID_DOES_NOT_EXIST.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot delete account that has not signed transaction")
    void canNotDeleteAccountThatHasNotSignedTransaction() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new AccountDeleteTransaction()
                    .setAccountId(accountId)
                    .setTransferAccountId(operatorId)
                    .setTransactionId(TransactionId.generate(accountId))
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_SIGNATURE.toString()));

            client.close();
        });
    }
}
