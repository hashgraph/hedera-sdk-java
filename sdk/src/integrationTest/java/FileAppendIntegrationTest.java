import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileContentsQuery;
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

public class FileAppendIntegrationTest {
    @Test
    @DisplayName("Can append to file")
    void canAppendToFile() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        var testEnv = new IntegrationTestEnv(1);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        @Var var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(28, info.size);
        assertFalse(info.isDeleted);
        assertNotNull(info.keys);
        assertNull(info.keys.getThreshold());
        assertEquals(KeyList.of(testEnv.operatorKey), info.keys);

        new FileAppendTransaction()
            .setFileId(fileId)
            .setContents("[e2e::FileAppendTransaction]")
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(56, info.size);
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
    @DisplayName("Can append large contents to file")
    void canAppendLargeContentsToFile() throws Exception {
        // There are potential bugs in FileAppendTransaction which require more than one node to trigger.
        var testEnv = new IntegrationTestEnv(2);

        var response = new FileCreateTransaction()
            .setKeys(testEnv.operatorKey)
            .setContents("[e2e::FileCreateTransaction]")
            .execute(testEnv.client);

        var fileId = Objects.requireNonNull(response.getReceipt(testEnv.client).fileId);

        Thread.sleep(5000);

        @Var var info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(28, info.size);
        assertFalse(info.isDeleted);
        assertNotNull(info.keys);
        assertNull(info.keys.getThreshold());
        assertEquals(KeyList.of(testEnv.operatorKey), info.keys);

        new FileAppendTransaction()
            .setFileId(fileId)
            .setContents(Contents.BIG_CONTENTS)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var contents = new FileContentsQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals("[e2e::FileCreateTransaction]" + Contents.BIG_CONTENTS, contents.toStringUtf8());

        info = new FileInfoQuery()
            .setFileId(fileId)
            .execute(testEnv.client);

        assertEquals(fileId, info.fileId);
        assertEquals(13522, info.size);
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
}
