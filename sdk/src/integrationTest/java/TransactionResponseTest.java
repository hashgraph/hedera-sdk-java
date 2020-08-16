import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TransactionResponseTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorId();
            Assert.assertNotNull(operatorId);

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .execute(client);

            var record = transaction.transactionId.getRecord(client);

            assertArrayEquals(record.transactionHash.toByteArray(), transaction.transactionHash);

            var accountId = record.receipt.accountId;
            Assert.assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }
}

