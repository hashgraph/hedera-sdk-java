import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class AccountIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var operatorKey = PrivateKey.fromString("302e020100300506032b65700422042091dad4f120ca225ce66deb1d6fb7ecad0e53b5e879aa45b0c5e0db7923f26d08");
            var operatorId = new AccountId(147722);

            var client = Client.forTestnet()
                .setOperator(operatorId, operatorKey);

            var key1 = PrivateKey.generate();
            var key2 = PrivateKey.generate();

            @Var var transactionId = new AccountCreateTransaction()
                .setKey(key1)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client);

            var receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertNotNull(receipt.accountId);
            assertEquals(receipt.status, Status.Success);
            assertTrue(Objects.requireNonNull(receipt.accountId).num > 0);

            var account = receipt.accountId;

            @Var var balance = new AccountBalanceQuery()
                .setAccountId(account)
                .execute(client);

            assertEquals(balance, new Hbar(1));

            var info = new AccountInfoQuery()
                .setAccountId(account)
                .execute(client);

            assertEquals(info.accountId, account);
            assertFalse(info.deleted);
            assertEquals(info.key.toString(), key1.toString());
            assertEquals(info.balance, new Hbar(1));
            assertEquals(info.autoRenewPeriod, Duration.ofDays(90));
            assertEquals(info.receiveRecordThreshold.asTinybar(), Long.MAX_VALUE);
            assertEquals(info.sendRecordThreshold.asTinybar(), Long.MAX_VALUE);
            assertNull(info.proxyAccountId);
            assertEquals(info.proxyReceived, Hbar.ZERO);

            new AccountRecordsQuery()
                .setAccountId(account)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertThrows(Exception.class, () -> {
                new AccountStakersQuery()
                    .setAccountId(account)
                    .setMaxQueryPayment(new Hbar(1))
                    .execute(client);
            });

            transactionId = new AccountUpdateTransaction()
                .setAccountId(account)
                .setKey(key2.getPublicKey())
                .setMaxTransactionFee(new Hbar(1))
                .build(client)
                .sign(key1)
                .sign(key2)
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            balance = new AccountBalanceQuery()
                .setAccountId(account)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertTrue(balance.asTinybar() < new Hbar(1).asTinybar());

            transactionId = new AccountDeleteTransaction()
                .setDeleteAccountId(account)
                .setTransferAccountId(operatorId)
                .setMaxTransactionFee(Hbar.fromTinybar(balance.asTinybar() / 2))
                .setTransactionId(TransactionId.generate(account))
                .build(client)
                .sign(key2)
                .execute(client);

            new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertThrows(Exception.class, () -> {
                new AccountInfoQuery()
                    .setAccountId(account)
                    .execute(client);
            });
        });
    }
}
