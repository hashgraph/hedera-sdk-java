import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.FileUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorKey = client.getOperatorKey();

            var response = new FileCreateTransaction()
                .setKeys(operatorKey)
                .setContents("[e2e::FileCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            var receipt = response.transactionId.getReceipt(client);

            assertNotNull(receipt.fileId);
            assertTrue(Objects.requireNonNull(receipt.fileId).num > 0);

            var file = receipt.fileId;

            @Var var info = new FileInfoQuery()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setQueryPayment(new Hbar(22))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.deleted);
            assertEquals(info.keys.get(0).toString(), Objects.requireNonNull(operatorKey).toString());

            new FileAppendTransaction()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setContents("[e2e::FileAppendTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .transactionId
                .getReceipt(client);

            info = new FileInfoQuery()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 56);
            assertFalse(info.deleted);
            assertEquals(info.keys.get(0).toString(), Objects.requireNonNull(operatorKey).toString());

            var contents = new FileContentsQuery()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction][e2e::FileAppendTransaction]");

            new FileUpdateTransaction()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setContents("[e2e::FileUpdateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .transactionId
                .getReceipt(client);

            info = new FileInfoQuery()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.deleted);
            assertEquals(info.keys.get(0).toString(), Objects.requireNonNull(operatorKey).toString());

            new FileDeleteTransaction()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .transactionId
                .getReceipt(client);

            info = new FileInfoQuery()
                .setNodeId(response.nodeId)
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertTrue(info.deleted);
            assertEquals(info.keys.get(0).toString(), Objects.requireNonNull(operatorKey).toString());

            client.close();
        });
    }
}
