import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import com.hedera.hashgraph.sdk.TransactionRecordQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ReceiptQueryIntegrationTest {
    @Test
    @DisplayName("Can get Receipt")
    void canGetTransactionReceipt() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        var receipt = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        testEnv.close(receipt.accountId, key);
    }

    @Test
    @DisplayName("Can get Record")
    void canGetTransactionRecord() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        var record = new TransactionRecordQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        testEnv.close(record.receipt.accountId, key);
    }

    @Test
    @DisplayName("Can get Record cost")
    void getCostTransactionRecord() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        var recordQuery = new TransactionRecordQuery()
            .setTransactionId(response.transactionId);

        var cost = recordQuery.getCost(testEnv.client);

        var record = recordQuery.execute(testEnv.client);

        testEnv.close(record.receipt.accountId, key);
    }

    @Test
    @DisplayName("Can get Record cost with big max set")
    void getCostBigMaxTransactionRecord() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        var recordQuery = new TransactionRecordQuery()
            .setTransactionId(response.transactionId)
            .setMaxQueryPayment(new Hbar(1000));

        var cost = recordQuery.getCost(testEnv.client);

        var record = recordQuery.execute(testEnv.client);

        testEnv.close(record.receipt.accountId, key);
    }

    @Test
    @DisplayName("Error at very small max, getRecord")
    void getCostSmallMaxTransactionRecord() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        var receipt = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        var recordQuery = new TransactionRecordQuery()
            .setTransactionId(response.transactionId)
            .setMaxQueryPayment(Hbar.fromTinybars(1));

        var cost = recordQuery.getCost(testEnv.client);

        var error = assertThrows(RuntimeException.class, () -> {
            recordQuery.execute(testEnv.client);
        });

        assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TransactionRecordQuery, of " + cost.toString() + ", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

        testEnv.close(receipt.accountId, key);
    }

    @Test
    @DisplayName("Insufficient transaction fee error for transaction record query")
    void getCostInsufficientTxFeeTransactionRecord() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        var receipt = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(testEnv.client);

        var recordQuery = new TransactionRecordQuery()
            .setTransactionId(response.transactionId);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            recordQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
        });

        assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

        testEnv.close(receipt.accountId, key);
    }
}
