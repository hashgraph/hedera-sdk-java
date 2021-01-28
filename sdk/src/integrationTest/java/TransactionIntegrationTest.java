import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.Assert;
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
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .freezeWith(client)
                .signWithOperator(client);

            var expectedHash = transaction.getTransactionHashPerNode();

            var response = transaction.execute(client);

            var record = response.getRecord(client);

            assertArrayEquals(expectedHash.get(response.nodeId), record.transactionHash.toByteArray());

            var accountId = record.receipt.accountId;
            assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
