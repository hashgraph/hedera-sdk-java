import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.KeyList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class FileCreateIntegrationTest {
    @Test
    @DisplayName("Can create file")
    void canCreateFile() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(28, info.size);
        assertFalse(info.isDeleted);
        assertNotNull(info.keys);
        assertNull(info.keys.getThreshold());
        assertEquals(KeyList.of(testEnv.operatorKey), info.keys);

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can create file with no contents")
    void canCreateFileWithNoContents() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(0, info.size);
        assertFalse(info.isDeleted);
        assertNotNull(info.keys);
        assertNull(info.keys.getThreshold());
        assertEquals(KeyList.of(testEnv.operatorKey), info.keys);

        new FileDeleteTransaction()
            .setFileId(fileId)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can create file with no keys")
    void canCreateFileWithNoKeys() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(0, info.size);
        assertFalse(info.isDeleted);
        assertNull(info.keys);

        testEnv.close();
    }
}
