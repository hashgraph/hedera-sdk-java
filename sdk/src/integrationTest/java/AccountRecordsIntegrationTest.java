import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountRecordsQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRecordsIntegrationTest {
    @Test
    @DisplayName("Can query account records")
    void canQueryAccountRecords() throws Exception {
        var testEnv = new IntegrationTestEnv(1);
        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(1).negated())
            .addHbarTransfer(accountId, new Hbar(1))
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(1))
            .addHbarTransfer(accountId, new Hbar(1).negated())
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var records = new AccountRecordsQuery()
            .setAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        assertTrue(!records.isEmpty());

        testEnv.close(accountId, key);
    }
}
