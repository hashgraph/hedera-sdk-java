import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

@Disabled
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
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
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
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
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
                .setInitialBalance(new Hbar(10));

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
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
                .setAdminKey(operatorKey)
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .setPayerAccountId(operatorId)
                .execute(client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .execute(client);

            var signature = key.sign(info.transactionBody);

            new ScheduleSignTransaction()
                .addScheduledSignature(key.getPublicKey(), signature)
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
            .setTopicId(topicId)
            .setMessage("scheduled hcs message".getBytes(StandardCharsets.UTF_8));

        // create schedule
        var scheduled = transaction.schedule()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAdminKey(operatorKey)
            .setPayerAccountId(operatorId)
            .setScheduleMemo("mirror scheduled E2E signature on create and sign_" + Instant.now())
            .freezeWith(client);

        var transactionId = scheduled.getTransactionId();

        var scheduleId = Objects.requireNonNull(scheduled
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

        var infoTransaction = (TopicMessageSubmitTransaction) info.getTransaction();

        assertEquals(transaction.getTopicId(), infoTransaction.getTopicId());
        assertEquals(transaction.getMessage(), infoTransaction.getMessage());
        assertEquals(transaction.getNodeAccountIds(), infoTransaction.getNodeAccountIds());

        var key2Signature = key2.sign(info.transactionBody);

        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .addScheduledSignature(key2.getPublicKey(), key2Signature)
            .execute(client)
            .getReceipt(client);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_SCHEDULE_ID.toString()));

        System.out.println(
            "https://previewnet.mirrornode.hedera.com/api/v1/transactions/" +
                transactionId.accountId.toString() +
                "-" +
                transactionId.validStart.getEpochSecond() +
                "-" +
                transactionId.validStart.getNano()
        );
    }
}
