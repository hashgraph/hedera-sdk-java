import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FileInfoIntegrationTest {
    @Test
    @DisplayName("Can query file info")
    void canQueryFileInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(operatorKey));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can query file info with no admin key or contents")
    void canQueryFileInfoWithNoAdminKeyOrContents() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var response = new FileCreateTransaction().execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 0);
            assertFalse(info.isDeleted);
            assertNull(info.keys);

            client.close();
        });
    }

    @Test
    @DisplayName("Can get cost, even with a big max")
    void getCostBigMaxQueryFileInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var infoQuery = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(new Hbar(1000));

            var cost = infoQuery.getCost(client);

            var info = infoQuery.setQueryPayment(cost).execute(client);

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Error, max is smaller than set payment.")
    void getCostSmallMaxQueryFileInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var infoQuery = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = infoQuery.getCost(client);

            var error = assertThrows(RuntimeException.class, () -> {
                infoQuery.execute(client);
            });

            assertEquals(error.getMessage(), "com.hedera.hashgraph.sdk.MaxQueryPaymentExceededException: cost for FileInfoQuery, of "+cost.toString()+", without explicit payment is greater than the maximum allowed payment of 1 tℏ");


            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Insufficient tx fee error.")
    void getCostInsufficientTxFeeQueryFileInfo() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var infoQuery = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxQueryPayment(Hbar.fromTinybars(1));

            var cost = infoQuery.getCost(client);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                infoQuery.setQueryPayment(Hbar.fromTinybars(1)).execute(client);
            });

            assertEquals(error.status.toString(), "INSUFFICIENT_TX_FEE");

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
