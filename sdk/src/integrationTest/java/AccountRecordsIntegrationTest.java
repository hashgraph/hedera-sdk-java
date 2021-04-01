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
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorAccountId();
            assertNotNull(operatorId);

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addHbarTransfer(operatorId, new Hbar(1).negated())
                .addHbarTransfer(accountId, new Hbar(1))
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addHbarTransfer(operatorId, new Hbar(1))
                .addHbarTransfer(accountId, new Hbar(1).negated())
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            var records = new AccountRecordsQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(operatorId)
                .execute(client);

            assertTrue(records.isEmpty());

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
