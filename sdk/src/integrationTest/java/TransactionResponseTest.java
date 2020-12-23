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

public class TransactionResponseTest {
    @Test
    @DisplayName("transaction hash in transaction record is equal to the transaction response transaction hash")
    void transactionHashInTransactionRecordIsEqualToTheTransactionResponseTransactionHash() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var transaction = new AccountCreateTransaction()
                .setKey(key)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            var record = transaction.getRecord(client);

            assertArrayEquals(record.transactionHash.toByteArray(), transaction.transactionHash);

            var accountId = record.receipt.accountId;
            assertNotNull(accountId);

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client);

            client.close();
        });
    }
}

