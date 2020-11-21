import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FileAppendIntegrationTest {
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

            @Var var info = new FileInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setFileId(file)
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys.getThreshold());
            var testKey = KeyList.of(Objects.requireNonNull(operatorKey)).setThreshold(info.keys.getThreshold());
            assertEquals(info.keys.toString(), testKey.toString());

            new FileAppendTransaction()
                .setFileId(file)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setContents("[e2e::FileAppendTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(file)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 56);
            assertFalse(info.isDeleted);
            assertNotNull(info.keys.getThreshold());
            testKey.setThreshold(info.keys.getThreshold());
            assertEquals(info.keys.toString(), testKey.toString());

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
