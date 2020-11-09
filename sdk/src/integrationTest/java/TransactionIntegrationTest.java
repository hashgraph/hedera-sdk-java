import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TransactionIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorAccountId();
            Assert.assertNotNull(operatorId);

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .setMaxTransactionFee(new Hbar(2))
                .freezeWith(client)
                .signWithOperator(client);

            var expectedHash = transaction.getTransactionHashPerNode();

            var response = transaction.execute(client);

            var record = response.getRecord(client);

            assertArrayEquals(expectedHash.get(response.nodeId), record.transactionHash.toByteArray());

            var accountId = record.receipt.accountId;
            Assert.assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .setMaxTransactionFee(new Hbar(1))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .transactionId
                .getReceipt(client);

            client.close();
        });
    }
}
