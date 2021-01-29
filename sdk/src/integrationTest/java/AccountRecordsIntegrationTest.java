import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordsIntegrationTest {
    @Test
    @DisplayName("Can query account records")
    void canQueryAccountRecords() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorAccountId();
            assertNotNull(operatorId);

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addHbarTransfer(operatorId, new Hbar(1).negated())
                .addHbarTransfer(accountId, new Hbar(1))
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addHbarTransfer(operatorId, new Hbar(1))
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            var records = new AccountRecordsQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(operatorId)
                .execute(client);

            assertTrue(records.isEmpty());

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
    @DisplayName("Can get cost, even with a big max")
    void getCostBigMaxAccountRecordsForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var records = new AccountRecordsQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(Hbar.MAX);

            var cost = records.getCost(client);

            var accrecords = records.setQueryPayment(cost).execute(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Error, max is smaller than set payment.")
    void getCostSmallMaxAccountRecordsForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var records = new AccountRecordsQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = records.getCost(client);

            var error = assertThrows(RuntimeException.class, () -> {
                records.execute(client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for AccountRecordsQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            client.close();
        });
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeAccountRecordsForClientOperator() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var records = new AccountRecordsQuery()
                .setAccountId(operatorId)
                .setMaxQueryPayment(Hbar.fromTinybars(10000));

            var cost = records.getCost(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                records.setQueryPayment(Hbar.fromTinybars(1)).execute(client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            client.close();
        });
    }
}
