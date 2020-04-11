import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.FileAppendTransaction;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileCreateTransaction;
import com.hedera.hashgraph.sdk.FileDeleteTransaction;
import com.hedera.hashgraph.sdk.FileInfoQuery;
import com.hedera.hashgraph.sdk.FileUpdateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var operatorKey = PrivateKey.fromString("302e020100300506032b6570042204207ce25f7ac7a4fa7284efa8453f153922e16ede6004c36778d3870c93d5dfbee5");
            var operatorId = new AccountId(1035);

            var client = Client.forTestnet()
                .setOperator(operatorId, operatorKey);

            @Var var transactionId = new FileCreateTransaction()
                .setKeys(operatorKey.getPublicKey())
                .setContents("[e2e::FileCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            @Var var receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertEquals(Status.Success, receipt.status);
            assertNotNull(receipt.fileId);
            assertTrue(Objects.requireNonNull(receipt.fileId).num > 0);

            var file = receipt.fileId;

            @Var var info = new FileInfoQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(22))
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

            receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertEquals(Status.Success, receipt.status);

            info = new FileInfoQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 56);
            assertFalse(info.deleted);
            assertEquals(info.keys[0].toString(), operatorKey.getPublicKey().toString());

            var contents = new FileContentsQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction][e2e::FileAppendTransaction]");

            transactionId = new FileUpdateTransaction()
                .setFileId(file)
                .setContents("[e2e::FileUpdateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertEquals(Status.Success, receipt.status);

            info = new FileInfoQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.deleted);
            assertEquals(info.keys[0].toString(), operatorKey.getPublicKey().toString());

            transactionId = new FileDeleteTransaction()
                .setFileID(file)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client);

            receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertEquals(Status.Success, receipt.status);

            assertThrows(Exception.class, () -> {
                new FileInfoQuery()
                    .setFileId(file)
                    .setQueryPayment(new Hbar(1))
                    .execute(client);
            });
        });
    }
}
