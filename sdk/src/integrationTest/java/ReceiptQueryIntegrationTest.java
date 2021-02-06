import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class ReceiptQueryIntegrationTest {
    @Test
    @DisplayName("Can get Receipt")
    void canGetTransactionReceipt() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            new AccountDeleteTransaction()
                .setAccountId(receipt.accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get Record")
    void canGetTransactionRecord() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            var record = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            new AccountDeleteTransaction()
                .setAccountId(record.receipt.accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get Record cost")
    void getCostTransactionRecord() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId));

            var cost = recordQuery.getCost(client);

            var record = recordQuery.execute(client);

            new AccountDeleteTransaction()
                .setAccountId(record.receipt.accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get Record cost with big max set")
    void getCostBigMaxTransactionRecord() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(new Hbar(1000));

            var cost = recordQuery.getCost(client);

            var record = recordQuery.execute(client);

            new AccountDeleteTransaction()
                .setAccountId(record.receipt.accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Error at very small max, getRecord")
    void getCostSmallMaxTransactionRecord() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = recordQuery.getCost(client);

            var error = assertThrows(RuntimeException.class, () -> {
                recordQuery.execute(client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for TransactionRecordQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");


            new AccountDeleteTransaction()
                .setAccountId(receipt.accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Insufficient transaction fee error for transactiong record query")
    void getCostInsufficientTxFeeTransactionRecord() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            var recordQuery = new TransactionRecordQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId));

            var cost = recordQuery.getCost(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                recordQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            new AccountDeleteTransaction()
                .setAccountId(receipt.accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }
}
