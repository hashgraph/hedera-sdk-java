import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRecordsIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorAccountId();
            assertNotNull(operatorId);

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var receipt = response.getReceipt(client);

            assertNotNull(receipt.accountId);
            assertTrue(Objects.requireNonNull(receipt.accountId).num > 0);

            var account = receipt.accountId;

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addHbarTransfer(operatorId, new Hbar(1).negated())
                .addHbarTransfer(account, new Hbar(1))
                .execute(client);

            var records = new AccountRecordsQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(operatorId)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertTrue(records.isEmpty());

            new AccountDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(account)
                .setTransferAccountId(operatorId)
                .setTransactionId(TransactionId.generate(account))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
