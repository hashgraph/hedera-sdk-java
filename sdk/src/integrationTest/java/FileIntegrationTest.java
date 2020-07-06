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
            @Var Client c;

            try {
                c = Client.fromJsonFile("./src/integrationTest/resources/client-config-with-operator.json");
                System.out.println("Using client from config file");
            } catch (Exception e) {
                System.out.println("Failed to use client network. Using testnet instead.");
                c = Client.forTestnet();
            }

            try {
                var operatorKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
                var operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

                c.setOperator(operatorId, operatorKey);
            } catch (Exception e) {
                System.out.println("Did not find `OPERATOR_KEY` or `OPERATOR_ID` environment variables.");
                System.out.println("Using operator within the config.");
            }

            var client = c;

            assertNotNull(client.getOperatorId());
            assertNotNull(client.getOperatorKey());

            var receipt = new FileCreateTransaction()
                .setKeys(client.getOperatorKey())
                .setContents("[e2e::FileCreateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

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
            assertEquals(info.keys.get(0).toString(), client.getOperatorKey().toString());

            new FileAppendTransaction()
                .setFileId(file)
                .setContents("[e2e::FileAppendTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 56);
            assertFalse(info.deleted);
            assertEquals(info.keys.get(0).toString(), client.getOperatorKey().toString());

            var contents = new FileContentsQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(contents.toStringUtf8(), "[e2e::FileCreateTransaction][e2e::FileAppendTransaction]");

            new FileUpdateTransaction()
                .setFileId(file)
                .setContents("[e2e::FileUpdateTransaction]")
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertEquals(info.size, 28);
            assertFalse(info.deleted);
            assertEquals(info.keys.get(0).toString(), client.getOperatorKey().toString());

            new FileDeleteTransaction()
                .setFileId(file)
                .setMaxTransactionFee(new Hbar(5))
                .execute(client)
                .getReceipt(client);

            info = new FileInfoQuery()
                .setFileId(file)
                .setQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(info.fileId, file);
            assertTrue(info.deleted);
            assertEquals(info.keys.get(0).toString(), client.getOperatorKey().toString());
        });
    }
}
