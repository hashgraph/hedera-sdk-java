import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountRecordsQuery;
import com.hedera.hashgraph.sdk.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRecordsIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorAccountId();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var receipt = response.transactionId.getReceipt(client);

            assertNotNull(receipt.accountId);
            assertTrue(Objects.requireNonNull(receipt.accountId).num > 0);

            var account = receipt.accountId;

            new CryptoTransferTransaction()
                .setNodeId(response.nodeId)
                .addRecipient(account, new Hbar(1))
                .addSender(operatorId, new Hbar(1))
                .execute(client);

            var records = new AccountRecordsQuery()
                .setNodeId(response.nodeId)
                .setAccountId(operatorId)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertTrue(records.isEmpty());

            new AccountDeleteTransaction()
                .setNodeId(response.nodeId)
                .setAccountId(account)
                .setTransferAccountId(operatorId)
                .setTransactionId(TransactionId.generate(account))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .transactionId
                .getReceipt(client);

            client.close();
        });
    }
}
