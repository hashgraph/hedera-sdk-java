import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleCreateIntegrationTest {
    @Test
    @DisplayName("Can create schedule")
    void canCreateSchedule() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10))
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .freezeWith(client);

            var response = new ScheduleCreateTransaction()
                .setTransaction(transaction)
                .setAdminKey(operatorKey)
                .setPayerAccountId(operatorId)
                .execute(client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            new ScheduleDeleteTransaction()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get Transaction")
    void canGetTransactionSchedule() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10))
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .freezeWith(client);

            var response = new ScheduleCreateTransaction()
                .setTransaction(transaction)
                .setAdminKey(operatorKey)
                .setPayerAccountId(operatorId)
                .execute(client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertNotNull(info.getTransaction());

            new ScheduleDeleteTransaction()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can create schedule with schedule()")
    void canCreateWithSchedule() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10))
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .freezeWith(client);

            var tx = transaction.schedule();

            var response = tx
                .setAdminKey(operatorKey)
                .setPayerAccountId(operatorId)
                .execute(client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            new ScheduleDeleteTransaction()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10))
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .freezeWith(client);

            var tx = transaction.schedule();

            var response = tx
                .setAdminKey(operatorKey)
                .setPayerAccountId(operatorId)
                .execute(client);

            var signature = key.signTransaction(transaction);

            var scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            var signTx = new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            new ScheduleDeleteTransaction()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key.getPublicKey())
                .setInitialBalance(new Hbar(10))
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .freezeWith(client);

            var response = new ScheduleCreateTransaction()
                .setTransaction(transaction)
                .setAdminKey(operatorKey)
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .setPayerAccountId(operatorId)
                .execute(client);

            var signature = key.signTransaction(transaction);

            var scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);

            var signTransaction = new ScheduleSignTransaction()
                .addScheduleSignature(key.getPublicKey(), signature)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setScheduleId(scheduleId)
                .freezeWith(client);

            new ScheduleDeleteTransaction()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
