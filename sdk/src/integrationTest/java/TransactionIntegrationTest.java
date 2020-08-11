import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TransactionIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorId();
            Assert.assertNotNull(operatorId);

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountId(new AccountId(5))
                .setMaxTransactionFee(new Hbar(2))
                .build(client)
                .signWithOperator(client);

            var expectedHash = transaction.hash();

            var txid = transaction.execute(client);

            var record = txid.getRecord(client);

            assertArrayEquals(expectedHash, record.transactionHash.toByteArray());

            var accountId = record.receipt.accountId;
            Assert.assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .setMaxTransactionFee(new Hbar(1))
                .build(client)
                .sign(key)
                .execute(client)
                .transactionId
                .getReceipt(client);

            client.close();
        });
    }
}
