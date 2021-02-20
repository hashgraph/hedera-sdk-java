import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountInfo;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

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
    @DisplayName("Can get cost for account info query")
    void getCostAccountInfoForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var info = new AccountInfoQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(new Hbar(1));

            var cost = info.getCost(client);

            var accInfo = info.setQueryPayment(cost).execute(client);

            assertEquals(accInfo.accountId, operatorId);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for account info query, with a bix max")
    void getCostBigMaxAccountInfoForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var info = new AccountInfoQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(Hbar.MAX);

            var cost = info.getCost(client);

            var accInfo = info.setQueryPayment(cost).execute(client);

            assertEquals(accInfo.accountId, operatorId);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost for account info query, with a small max")
    void getCostSmallMaxAccountInfoForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var info = new AccountInfoQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = info.getCost(client);

            var error = assertThrows(RuntimeException.class, () -> {
                info.execute(client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for AccountInfoQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            client.close();
        });
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeAccountInfoForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var info = new AccountInfoQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(Hbar.fromTinybars(10000));

            var cost = info.getCost(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                info.setQueryPayment(Hbar.fromTinybars(1)).execute(client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

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
