import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FileUpdateIntegrationTest {
    @Test
    @DisplayName("Can update file")
    void canUpdateFile() {
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
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNotNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(operatorKey).setThreshold(info.keys.getThreshold()));

            new FileUpdateTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setContents("[e2e::FileUpdateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(1))
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
    @DisplayName("Cannot update immutable file")
    void cannotUpdateImmutableFile() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var response = new FileCreateTransaction()
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            var info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNull(info.keys);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new FileUpdateTransaction()
                    .setFileId(fileId)
                    .setNodeAccountIds(Collections.singletonList(response.nodeId))
                    .setContents("[e2e::FileUpdateTransaction]")
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.UNAUTHORIZED.toString()));

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot update file when file ID is not set")
    void cannotUpdateFileWhenFileIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new FileUpdateTransaction()
                    .setContents("[e2e::FileUpdateTransaction]")
                    .execute(client)
                    .getReceipt(client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_FILE_ID.toString()));

            client.close();
        });
    }
}
