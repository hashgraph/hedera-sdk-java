import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountRecordsIntegrationTest {
    @Test
    @DisplayName("Can query account records")
    void canQueryAccountRecords() {
        assertDoesNotThrow(() -> {
            var testEnv = new IntegrationTestEnv();
            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addHbarTransfer(testEnv.operatorId, new Hbar(1).negated())
                .addHbarTransfer(accountId, new Hbar(1))
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            new TransferTransaction()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .addHbarTransfer(testEnv.operatorId, new Hbar(1))
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            var records = new AccountRecordsQuery()
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setAccountId(testEnv.operatorId)
                .execute(testEnv.client);

            assertTrue(!records.isEmpty());

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(testEnv.nodeAccountIds)
                .setTransferAccountId(testEnv.operatorId)
                .freezeWith(testEnv.client)
                .sign(key)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);

            testEnv.client.close();
        });
    }
}
