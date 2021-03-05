import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
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

public class FileAppendIntegrationTest {
    @Test
    @DisplayName("Can append to file")
    void canAppendToFile() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var info = new FileInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setFileId(fileId)
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(operatorKey));

            new FileAppendTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setContents("[e2e::FileAppendTransaction]")
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 56);
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
    @DisplayName("Can append large contents to file")
    void canAppendLargeContentsToFile() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            @Var var info = new FileInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setFileId(fileId)
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys);
            assertNull(info.keys.getThreshold());
            assertEquals(info.keys, KeyList.of(operatorKey));

            new FileAppendTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setContents(Contents.BIG_CONTENTS)
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(info.fileId, fileId);
            assertEquals(info.size, 13522);
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
}
