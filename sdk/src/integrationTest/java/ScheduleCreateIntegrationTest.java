import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
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

            new ScheduleInfoQuery()
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

            new ScheduleInfoQuery()
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
    void canSignSchedule2() {
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

            new ScheduleSignTransaction()
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

    @Test
    @DisplayName("Can schedule topic message")
    void canScheduleTopicMessage() throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = IntegrationTestClientManager.getClient();
        var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
        var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

        // Generate 3 random keys
        var key1 = PrivateKey.generate();

        // This is the submit key
        var key2 = PrivateKey.generate();

        var key3 = PrivateKey.generate();

        var keyList = new KeyList();

        keyList.add(key1.getPublicKey());
        keyList.add(key2.getPublicKey());
        keyList.add(key3.getPublicKey());

        var response = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(100))
            .setKey(keyList)
            .execute(client);

       assertNotNull(response.getReceipt(client).accountId);

        var topicId = Objects.requireNonNull(new TopicCreateTransaction()
            .setAdminKey(operatorKey)
            .setAutoRenewAccountId(operatorId)
            .setTopicMemo("HCS Topic_")
            .setSubmitKey(key2.getPublicKey())
            .execute(client)
            .getReceipt(client)
            .topicId
        );

        var transaction = new TopicMessageSubmitTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTopicId(topicId)
            .setMessage("scheduled hcs message".getBytes(StandardCharsets.UTF_8))
            .freezeWith(client);

        // create schedule
        var scheduleId = Objects.requireNonNull(transaction.schedule()
            .setAdminKey(operatorKey)
            .setPayerAccountId(operatorId)
            .setMemo("mirror scheduled E2E signature on create and sign_" + Instant.now())
            .execute(client)
            .getReceipt(client)
            .scheduleId
        );

        // verify schedule has been created and has 1 of 2 signatures
        var info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .execute(client);

        assertNotNull(info);
        assertEquals(info.scheduleId, scheduleId);

        var key2Signature = key2.signTransaction(transaction);

        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .addScheduleSignature(key2.getPublicKey(), key2Signature)
            .execute(client)
            .getReceipt(client);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_SCHEDULE_ID.toString()));
    }
}
