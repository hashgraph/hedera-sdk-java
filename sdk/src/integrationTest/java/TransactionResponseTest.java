import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransactionResponseTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the transaction response transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheTransactionResponseTransactionHash() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generate();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        var record = transaction.getRecord(testEnv.client);

        assertArrayEquals(record.transactionHash.toByteArray(), transaction.transactionHash);

        var accountId = record.receipt.accountId;
        assertNotNull(accountId);

        testEnv.close(accountId, key);
    }
}

