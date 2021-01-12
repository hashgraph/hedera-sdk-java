import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FileContentsIntegrationTest {
    @Test
    @DisplayName("Can query file contents")
    void canQueryFileContents() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            var contents = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction]");

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Can query empty file contents")
    void canQueryEmptyFileContents() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .execute(client);

            var fileId = Objects.requireNonNull(response.getReceipt(client).fileId);

            var contents = new FileContentsQuery()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client);

            assertEquals(contents.size(), 0);

            new FileDeleteTransaction()
                .setFileId(fileId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }

    @Test
    @DisplayName("Cannot query file contents when file ID is not set")
    void cannotQueryFileContentsWhenFileIDIsNotSet() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

           var error = assertThrows(PrecheckStatusException.class, () -> {
               new FileContentsQuery()
                   .execute(client);
           });

           assertTrue(error.getMessage().contains(Status.INVALID_FILE_ID.toString()));

            client.close();
        });
    }
}
