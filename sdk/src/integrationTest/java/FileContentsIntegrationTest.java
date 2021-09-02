import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileContentsIntegrationTest {
    @Test
    @DisplayName("Can query file contents")
    void canQueryFileContents() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var contents = new FileContentsQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction]");

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can query empty file contents")
    void canQueryEmptyFileContents() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var contents = new FileContentsQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(contents.size(), 0);

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Cannot query file contents when file ID is not set")
    void cannotQueryFileContentsWhenFileIDIsNotSet() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            new FileContentsQuery()
                .execute(testEnv.client);
        });

        assertTrue(error.getMessage().contains(Status.INVALID_FILE_ID.toString()));

        testEnv.close();
    }

    @Test
    @DisplayName("Can get cost, even with a big max")
    void getCostBigMaxQueryFileContents() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var contentsQuery = new FileContentsQuery()
            .setFileId(fileId)
            .setMaxQueryPayment(new Hbar(1000));

        var cost = contentsQuery.getCost(testEnv.client);

        var contents = contentsQuery.execute(testEnv.client);

        assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction]");

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Error, max is smaller than set payment.")
    void getCostSmallMaxQueryFileContents() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var contentsQuery = new FileContentsQuery()
            .setFileId(fileId)
            .setMaxQueryPayment(Hbar.fromTinybars(1));

        var cost = contentsQuery.getCost(testEnv.client);

        var error = assertThrows(RuntimeException.class, () -> {
            contentsQuery.execute(testEnv.client);
        });

        assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for FileContentsQuery, of " + cost.toString() + ", without explicit payment is greater than the maximum allowed payment of 1 tℏ");

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeQueryFileContents() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var contentsQuery = new FileContentsQuery()
            .setFileId(fileId)
            .setMaxQueryPayment(new Hbar(100));

        var cost = contentsQuery.getCost(testEnv.client);

        var error = assertThrows(PrecheckStatusException.class, () -> {
            contentsQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(testEnv.client);
        });

        assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }
}
