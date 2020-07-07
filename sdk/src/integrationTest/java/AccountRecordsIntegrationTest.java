import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountRecordsQuery;
import com.hedera.hashgraph.sdk.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountRecordsIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();

            var key = PrivateKey.generate();

            var receipt = new AccountCreateTransaction()
                .setKey(key)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .setReceiveRecordThreshold(Hbar.fromTinybars(1))
                .setSendRecordThreshold(Hbar.fromTinybars(1))
                .execute(client)
                .getReceipt(client);

            assertNotNull(receipt.accountId);
            assertTrue(Objects.requireNonNull(receipt.accountId).num > 0);

            var account = receipt.accountId;

            new CryptoTransferTransaction()
                .addRecipient(account, new Hbar(1))
                .addSender(client.getOperatorId(), new Hbar(1))
                .execute(client);

            var records = new AccountRecordsQuery()
                .setAccountId(client.getOperatorId())
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertTrue(records.isEmpty());

            new AccountDeleteTransaction()
                .setAccountId(account)
                .setTransferAccountId(client.getOperatorId())
                .setTransactionId(TransactionId.generate(account))
                .build(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);
        });
    }
}
