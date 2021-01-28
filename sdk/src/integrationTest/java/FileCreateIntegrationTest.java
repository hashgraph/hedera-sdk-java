import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FileCreateIntegrationTest {
    @Test
    @DisplayName("Can create file")
    void canCreateFile() {
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
            assertNotNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(operatorKey).setThreshold(info.keys.getThreshold()));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can create file with no contents")
    void canCreateFileWithNoContents() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 0);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNotNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(operatorKey).setThreshold(info.keys.getThreshold()));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can create file with no keys")
    void canCreateFileWithNoKeys() {
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
}
