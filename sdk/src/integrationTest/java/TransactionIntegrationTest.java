import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionIntegrationTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the derived transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheDerivedTransactionHash() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .freezeWith(testEnv.client)
                .signWithOperator(testEnv.client);

            var expectedHash = transaction.getTransactionHashPerNode();

            var response = transaction.execute(testEnv.client);

            var record = response.getRecord(testEnv.client);

            assertArrayEquals(expectedHash.get(response.nodeId), record.transactionHash.toByteArray());

            var accountId = record.receipt.accountId;
            assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(accountId)
                .setTransferAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }
}
