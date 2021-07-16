import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReceiptQueryIntegrationTest {
    @Test
    @DisplayName("Can get Receipt")
    void canGetTransactionReceipt() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            testEnv.cleanUpAndClose(receipt.accountId, key);
        });
    }

    @Test
    @DisplayName("Can get Record")
    void canGetTransactionRecord() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            var record = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            testEnv.cleanUpAndClose(record.receipt.accountId, key);
        });
    }

    @Test
    @DisplayName("Can get Record cost")
    void getCostTransactionRecord() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId);
                //.setNodeAccountIds(testEnv.nodeAccountIds);

            var cost = recordQuery.getCost(testEnv.client);

            var record = recordQuery.execute(testEnv.client);

            testEnv.cleanUpAndClose(record.receipt.accountId, key);
        });
    }

    @Test
    @DisplayName("Can get Record cost with big max set")
    void getCostBigMaxTransactionRecord() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = recordQuery.getCost(testEnv.client);

            var record = recordQuery.execute(testEnv.client);

            testEnv.cleanUpAndClose(record.receipt.accountId, key);
        });
    }

    @Test
    @DisplayName("Error at very small max, getRecord")
    void getCostSmallMaxTransactionRecord() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = recordQuery.getCost(testEnv.client);

            var error = assertThrows(RuntimeException.class, () -> {
                recordQuery.execute(testEnv.client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TransactionRecordQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            testEnv.cleanUpAndClose(receipt.accountId, key);
        });
    }

    @Test
    @DisplayName("Insufficient transaction fee error for transaction record query")
    void getCostInsufficientTxFeeTransactionRecord() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .execute(testEnv.client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId);
                //.setNodeAccountIds(testEnv.nodeAccountIds);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                recordQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            testEnv.cleanUpAndClose(receipt.accountId, key);
        });
    }
}
