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

public class FileDeleteIntegrationTest {
    @Test
    @DisplayName("Can delete file")
    void canDeleteFile() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new FileCreateTransaction()
                .setKeys(testEnv.operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            @Var var info = new FileInfoQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(testEnv.operatorKey));

            new FileDeleteTransaction()
                .setFileId(fileId)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.close();
        });
    }

    @Test
    @DisplayName("Cannot delete immutable file")
    void cannotDeleteImmutableFile() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv(1);

            var response = new FileCreateTransaction()
                .setContents("[e2e::FileCreateTransaction]")
                .execute(testEnv.client);

            var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

            @Var var info = new FileInfoQuery()
                .setFileId(fileId)
                .execute(testEnv.client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNull(info.keys);

            var error = assertThrows(ReceiptStatusException.class, () -> {
                new FileDeleteTransaction()
                    .setFileId(fileId)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.UNAUTHORIZED.toString()));

            testEnv.close();
        });
    }
}
