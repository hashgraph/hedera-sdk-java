import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.KeyList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class FileAppendIntegrationTest {
    @Test
    @DisplayName("Can append to file")
    void canAppendToFile() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            @Var var info = new FileInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .setFileId(fileId)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey.getPublicKey()));

            new FileAppendTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .setContents("[e2e::FileAppendTransaction]")
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 56);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey.getPublicKey()));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }

    @Test
    @DisplayName("Can append large contents to file")
    void canAppendLargeContentsToFile() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var response = new FileCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            @Var var info = new FileInfoQuery()
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .setFileId(fileId)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey.getPublicKey()));

            new FileAppendTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .setContents(Contents.BIG_CONTENTS)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 13522);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey.getPublicKey()));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(testEnv.nodeAccountIdsForChunked)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.cleanUpAndClose();
        });
    }
}
