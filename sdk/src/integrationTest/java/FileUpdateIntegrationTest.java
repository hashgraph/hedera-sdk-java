import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileUpdateIntegrationTest {
    @Test
    @DisplayName("Can update file")
    void canUpdateFile() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var response = new FileCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            @Var var info = new FileInfoQuery()
                .setFileId(fileId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey));

            new FileUpdateTransaction()
                .setFileId(fileId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setContents("[e2e::FileUpdateTransaction]")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey));

            new FileDeleteTransaction()
                .setFileId(fileId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot update immutable file")
    void cannotUpdateImmutableFile() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var response = new FileCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            var info = new FileInfoQuery()
                .setFileId(fileId)
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNull(info.keys);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new FileUpdateTransaction()
                    .setFileId(fileId)
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setContents("[e2e::FileUpdateTransaction]")
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.UNAUTHORIZED.toString()));

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Cannot update file when file ID is not set")
    void cannotUpdateFileWhenFileIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new FileUpdateTransaction()
                    //.setNodeAccountIds(testEnv.nodeAccountIds)
                    .setContents("[e2e::FileUpdateTransaction]")
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.INVALID_FILE_ID.toString()));

            testEnv.cleanUpAndClose();
        });
    }
}
