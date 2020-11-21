import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileContentsIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = client.getOperatorPublicKey();
            assertNotNull(operatorKey);

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            var receipt = response.getReceipt(client);

            assertNotNull(receipt.fileId);
            assertTrue(Objects.requireNonNull(receipt.fileId).num > 0);

            var file = receipt.fileId;

            var contents = new FileContentsQuery()
                .setFileId(file)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction]");

            new FileDeleteTransaction()
                .setFileId(file)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
