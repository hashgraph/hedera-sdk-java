import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileContentsIntegrationTest {
    @Test
    @DisplayName("Can query file contents")
    void canQueryFileContents() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var contents = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction]");

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can query empty file contents")
    void canQueryEmptyFileContents() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKeys(testEnv.operatorKey)
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var contents = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(contents.size(), 0);

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Cannot query file contents when file ID is not set")
    void cannotQueryFileContentsWhenFileIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

           var error = assertThrows(PrecheckStatusException.class, () -> {
               new FileContentsQuery()
                   .execute(testEnv.client);
           });

           assertTrue(error.getMessage().contains(Status.INVALID_FILE_ID.toString()));

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Can get cost, even with a big max")
    void getCostBigMaxQueryFileContents() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var contentsQuery = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(new Hbar(1000));

            var cost = contentsQuery.getCost(testEnv.client);

            var contents = contentsQuery.execute(testEnv.client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction]");

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Error, max is smaller than set payment.")
    void getCostSmallMaxQueryFileContents() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var contentsQuery = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = contentsQuery.getCost(testEnv.client);

            var error = assertThrows(RuntimeException.class, () -> {
                contentsQuery.execute(testEnv.client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for FileContentsQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeQueryFileContents() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var contentsQuery = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setMaxQueryPayment(new Hbar(100));

            var cost = contentsQuery.getCost(testEnv.client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                contentsQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }
}
