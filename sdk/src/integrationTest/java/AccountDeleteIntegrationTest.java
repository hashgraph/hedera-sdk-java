import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionId;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountDeleteIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = client.getOperatorId();

            var key = PrivateKey.generate();

            var receipt = new AccountCreateTransaction()
                .setKey(key)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client)
                .transactionId
                .getReceipt(client);

            assertNotNull(receipt.accountId);
            assertTrue(Objects.requireNonNull(receipt.accountId).num > 0);

            var account = receipt.accountId;

            var info = new AccountInfoQuery()
                .setAccountId(account)
                .execute(client);

            assertEquals(info.accountId, account);
            assertFalse(info.deleted);
            assertEquals(info.key.toString(), key.getPublicKey().toString());
            assertEquals(info.balance, new Hbar(1));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertEquals(info.receiveRecordThreshold.toTinybars(), Long.MAX_VALUE);
            assertEquals(info.sendRecordThreshold.toTinybars(), Long.MAX_VALUE);
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            new AccountDeleteTransaction()
                .setAccountId(account)
                .setTransferAccountId(operatorId)
                .setTransactionId(TransactionId.generate(account))
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .transactionId
                .getReceipt(client);

            client.close();
        });
    }
}
