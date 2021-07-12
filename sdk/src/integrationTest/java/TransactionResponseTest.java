import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TransactionResponseTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the transaction response transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheTransactionResponseTransactionHash() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            var record = transaction.getRecord(testEnv.client);

            assertArrayEquals(record.transactionHash.toByteArray(), transaction.transactionHash);

            var accountId = record.receipt.accountId;
            assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .setTransferAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client);

            testEnv.client.close();
        });
    }
}

