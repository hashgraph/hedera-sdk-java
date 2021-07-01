import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleCreateIntegrationTest {
    @Test
    @Disabled
    @DisplayName("Can create schedule")
    void canCreateSchedule() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertNotNull(info.executedAt);

            testEnv.client.close();
        });
    }

    @Test
    @Disabled
    @DisplayName("Can get Transaction")
    void canGetTransactionSchedule() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(10));

            var response = new ScheduleCreateTransaction()
                .setScheduledTransaction(transaction)
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertNotNull(info.executedAt);
            assertNotNull(info.getScheduledTransaction());

            testEnv.client.close();
        });
    }

    @Test
    @Disabled
    @DisplayName("Can create schedule with schedule()")
    void canCreateWithSchedule() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(10));

            var tx = transaction.schedule();

            var response = tx
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertNotNull(info.executedAt);
            assertNotNull(info.getScheduledTransaction());

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule2() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            PrivateKey key1 = PrivateKey.generate();
            PrivateKey key2 = PrivateKey.generate();
            PrivateKey key3 = PrivateKey.generate();

            KeyList keyList = new KeyList();

            keyList.add(key1.getPublicKey());
            keyList.add(key2.getPublicKey());
            keyList.add(key3.getPublicKey());

            // Creat the account with the `KeyList`
            TransactionResponse response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(keyList)
                .setInitialBalance(new Hbar(10))
                .execute(testEnv.client);

            // This will wait for the receipt to become available
            @Var TransactionReceipt receipt = response.getReceipt(testEnv.client);

            AccountId accountId = Objects.requireNonNull(receipt.accountId);

            // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
            // after we expect it to have been executed
            TransactionId transactionId = TransactionId.generate(testEnv.operatorId);

            // Create a transfer transaction with 2/3 signatures.
            TransferTransaction transfer = new TransferTransaction()
                .setTransactionId(transactionId)
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .addHbarTransfer(testEnv.operatorId, new Hbar(1));

            // Schedule the transactoin
            ScheduleCreateTransaction scheduled = transfer.schedule().setNodeAccountIds(testEnv.nodeAccountIds);

            receipt = scheduled.execute(testEnv.client).getReceipt(testEnv.client);

            // Get the schedule ID from the receipt
            ScheduleId scheduleId = Objects.requireNonNull(receipt.scheduleId);

            // Get the schedule info to see if `signatories` is populated with 2/3 signatures
            ScheduleInfo info = new ScheduleInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertNull(info.executedAt);

            // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
            // since all 3 signatures have been provided.
            ScheduleSignTransaction signTransaction = new ScheduleSignTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client);

            signTransaction.sign(key1).sign(key2).sign(key3).execute(testEnv.client).getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setScheduleId(scheduleId)
                .execute(testEnv.client);

            assertNotNull(info.executedAt);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can schedule topic message")
    void canScheduleTopicMessage() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

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
                .execute(testEnv.client);

            assertNotNull(response.getReceipt(testEnv.client).accountId);

            var topicId = Objects.requireNonNull(new TopicCreateTransaction()
                .setAdminKey(testEnv.operatorKey)
                .setAutoRenewAccountId(testEnv.operatorId)
                .setTopicMemo("HCS Topic_")
                .setSubmitKey(key2.getPublicKey())
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .topicId
            );

            var transaction = new TopicMessageSubmitTransaction()
                .setTopicId(topicId)
                .setMessage("scheduled hcs message".getBytes(StandardCharsets.UTF_8));

            // create schedule
            var scheduledTx = transaction.schedule()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAdminKey(testEnv.operatorKey)
                .setPayerAccountId(testEnv.operatorId)
                .setScheduleMemo("mirror scheduled E2E signature on create and sign_" + Instant.now());

            var scheduled = scheduledTx.freezeWith(testEnv.client);

            var transactionId = scheduled.getTransactionId();

            assertNotNull(transactionId);

            var scheduleId = Objects.requireNonNull(scheduled
                .execute(testEnv.client)
                .getReceipt(testEnv.client)
                .scheduleId
            );

            // verify schedule has been created and has 1 of 2 signatures
            var info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertNotNull(info);
            assertEquals(info.scheduleId, scheduleId);

            var infoTransaction = (TopicMessageSubmitTransaction) info.getScheduledTransaction();

            assertEquals(transaction.getTopicId(), infoTransaction.getTopicId());
            assertEquals(transaction.getNodeAccountIds(), infoTransaction.getNodeAccountIds());

            var scheduleSign = new ScheduleSignTransaction()
                .setScheduleId(scheduleId)
                .freezeWith(testEnv.client);

            scheduleSign
                .sign(key2)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new ScheduleInfoQuery()
                .setScheduleId(scheduleId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertNotNull(info.executedAt);
        });
    }
}
