import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleCreateIntegrationTest {
    @Test
    @Disabled
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

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertNotNull(info.executedAt);

            client.close();
        });
    }

    @Test
    @Disabled
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

            assertNotNull(info.executedAt);
            assertNotNull(info.getScheduledTransaction());

            client.close();
        });
    }

    @Test
    @Disabled
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

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertNotNull(info.executedAt);
            assertNotNull(info.getScheduledTransaction());

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

            PrivateKey key1 = PrivateKey.generate();
            PrivateKey key2 = PrivateKey.generate();
            PrivateKey key3 = PrivateKey.generate();

            KeyList keyList = new KeyList();

            keyList.add(key1.getPublicKey());
            keyList.add(key2.getPublicKey());
            keyList.add(key3.getPublicKey());

            // Creat the account with the `KeyList`
            TransactionResponse response = new AccountCreateTransaction()
                .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
                .setKey(keyList)
                .setInitialBalance(new Hbar(10))
                .execute(client);

            // This will wait for the receipt to become available
            @Var TransactionReceipt receipt = response.getReceipt(client);

            AccountId accountId = Objects.requireNonNull(receipt.accountId);

            // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
            // after we expect it to have been executed
            TransactionId transactionId = TransactionId.generate(operatorId);

            // Create a transfer transaction with 2/3 signatures.
            TransferTransaction transfer = new TransferTransaction()
                .setTransactionId(transactionId)
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(operatorId, new Hbar(1));

            // Schedule the transactoin
            ScheduleCreateTransaction scheduled = transfer.schedule();

            receipt = scheduled.execute(client).getReceipt(client);

            // Get the schedule ID from the receipt
            ScheduleId scheduleId = Objects.requireNonNull(receipt.scheduleId);

            // Get the schedule info to see if `signatories` is populated with 2/3 signatures
            ScheduleInfo info = new ScheduleInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setScheduleId(scheduleId)
                .execute(client);

            assertNull(info.executedAt);

            // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
            // since all 3 signatures have been provided.
            ScheduleSignTransaction signTransaction = new ScheduleSignTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setScheduleId(scheduleId)
                .freezeWith(client);

            signTransaction.sign(key1).sign(key2).sign(key3).execute(client).getReceipt(client);

            info = new ScheduleInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setScheduleId(scheduleId)
                .execute(client);

            assertNotNull(info.executedAt);

            client.close();
        });
    }

//    @Test
//    @DisplayName("Can schedule topic message")
//    void canScheduleTopicMessage() throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
//        Client client = IntegrationTestClientManager.getClient();
//        var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());
//        var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
//
//        // Generate 3 random keys
//        var key1 = PrivateKey.generate();
//
//        // This is the submit key
//        var key2 = PrivateKey.generate();
//
//        var key3 = PrivateKey.generate();
//
//        var keyList = new KeyList();
//
//        keyList.add(key1.getPublicKey());
//        keyList.add(key2.getPublicKey());
//        keyList.add(key3.getPublicKey());
//
//        var response = new AccountCreateTransaction()
//            .setInitialBalance(new Hbar(100))
//            .setKey(keyList)
//            .execute(client);
//
//        assertNotNull(response.getReceipt(client).accountId);
//
//        var topicId = Objects.requireNonNull(new TopicCreateTransaction()
//            .setAdminKey(operatorKey)
//            .setAutoRenewAccountId(operatorId)
//            .setTopicMemo("HCS Topic_")
//            .setSubmitKey(key2.getPublicKey())
//            .execute(client)
//            .getReceipt(client)
//            .topicId
//        );
//
//        var transaction = new TopicMessageSubmitTransaction()
//            .setTopicId(topicId)
//            .setMessage("scheduled hcs message".getBytes(StandardCharsets.UTF_8));
//
//        // create schedule
//        var scheduled = transaction.schedule()
//            .setNodeAccountIds(Collections.singletonList(response.nodeId))
//            .setAdminKey(operatorKey)
//            .setPayerAccountId(operatorId)
//            .setScheduleMemo("mirror scheduled E2E signature on create and sign_" + Instant.now())
//            .freezeWith(client);
//
//        var transactionId = scheduled.getTransactionId();
//
//        var scheduleId = Objects.requireNonNull(scheduled
//            .execute(client)
//            .getReceipt(client)
//            .scheduleId
//        );
//
//        // verify schedule has been created and has 1 of 2 signatures
//        var info = new ScheduleInfoQuery()
//            .setScheduleId(scheduleId)
//            .setNodeAccountIds(Collections.singletonList(response.nodeId))
//            .execute(client);
//
//        assertNotNull(info);
//        assertEquals(info.scheduleId, scheduleId);
//
//        var infoTransaction = (TopicMessageSubmitTransaction) info.getTransaction().schedulableTransaction;
//
//        assertEquals(transaction.getTopicId(), infoTransaction.getTopicId());
//        assertEquals(transaction.getMessage(), infoTransaction.getMessage());
//        assertEquals(transaction.getNodeAccountIds(), infoTransaction.getNodeAccountIds());
//
//        var scheduleSign = new ScheduleSignTransaction()
//            .setScheduleId(scheduleId)
//            .freezeWith(client);
//
//        scheduleSign
//            .sign(key2)
//            .execute(client)
//            .getReceipt(client);
//
//        var error = assertThrows(PrecheckStatusException.class, () -> {
//            new ScheduleInfoQuery()
//                .setScheduleId(scheduleId)
//                .setNodeAccountIds(Collections.singletonList(response.nodeId))
//                .execute(client);
//        });
//
//        assertTrue(error.getMessage().contains(Status.INVALID_SCHEDULE_ID.toString()));
//
//        System.out.println(
//            "https://previewnet.mirrornode.hedera.com/api/v1/transactions/" +
//                transactionId.accountId.toString() +
//                "-" +
//                transactionId.validStart.getEpochSecond() +
//                "-" +
//                transactionId.validStart.getNano()
//        );
//    }
}
