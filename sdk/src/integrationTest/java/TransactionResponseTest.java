import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TransactionResponseTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the transaction response transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheTransactionResponseTransactionHash() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .execute(testEnv.client);

        var record = transaction.getRecord(testEnv.client);

        assertThat(record.transactionHash.toByteArray()).containsExactly(transaction.transactionHash);

        var accountId = record.receipt.accountId;
        assertThat(accountId).isNotNull();

        testEnv.close(accountId, key);
    }
}

