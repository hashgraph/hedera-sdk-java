import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class FileIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var operatorKey = PrivateKey.fromString("302e020100300506032b65700422042091dad4f120ca225ce66deb1d6fb7ecad0e53b5e879aa45b0c5e0db7923f26d08");
            var operatorId = new AccountId(147722);

            var client = Client.forTestnet()
                .setOperator(operatorId, operatorKey);

            @Var var transactionId = new FileCreateTransaction()
                .addKey(operatorKey.getPublicKey())
                .setContents("[e2e::FileCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertNotNull(receipt.fileId);
            assertEquals(receipt.status, Status.Success);
            assertTrue(Objects.requireNonNull(receipt.fileId).num > 0);

            var file = receipt.fileId;

            @Var var info = new FileInfoQuery()
                .setFileId(file)
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.deleted);
            assertEquals(info.keys[0].toString(), operatorKey.getPublicKey().toString());

            transactionId = new FileAppendTransaction()
                .setFileId(file)
                .setContents("[e2e::FileAppendTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            info = new FileInfoQuery()
                .setFileId(file)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 56);
            assertFalse(info.deleted);
            assertEquals(info.keys[0].toString(), operatorKey.getPublicKey().toString());

            var contents = new FileContentsQuery()
                .setFileId(file)
                .execute(client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction][e2e::FileAppendTransaction]");

            transactionId = new FileUpdateTransaction()
                .setFileId(file)
                .setContents("[e2e::FileUpdateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            info = new FileInfoQuery()
                .setFileId(file)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.deleted);
            assertEquals(info.keys[0].toString(), operatorKey.getPublicKey().toString());

            transactionId = new FileDeleteTransaction()
                .setFileID(file)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertThrows(Exception.class, () -> {
                new FileInfoQuery()
                    .setFileId(file)
                    .setMaxQueryPayment(new Hbar(1))
                    .execute(client);
            });
        });
    }
}
