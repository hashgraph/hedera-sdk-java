import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountRecordsQuery;
import com.hedera.hashgraph.sdk.AccountStakersQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var operatorKey = PrivateKey.fromString("302e020100300506032b6570042204207ce25f7ac7a4fa7284efa8453f153922e16ede6004c36778d3870c93d5dfbee5");
            var operatorId = new AccountId(1035);

            var client = Client.forTestnet()
                .setOperator(operatorId, operatorKey);

            var key1 = PrivateKey.generate();
            var key2 = PrivateKey.generate();

            @Var var transactionId = new AccountCreateTransaction()
                .setKey(key1)
                .setMaxTransactionFee(new Hbar(2))
                .setInitialBalance(new Hbar(1))
                .execute(client);

            @Var var receipt = new TransactionReceiptQuery()
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
            assertEquals(info.key.toString(), key1.getPublicKey().toString());
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

            receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertEquals(Status.Success, receipt.status);

            balance = new AccountBalanceQuery()
                .setAccountId(account)
                .setMaxQueryPayment(new Hbar(1))
                .execute(client);

            assertEquals(balance, new Hbar(1));

            transactionId = new AccountDeleteTransaction()
                .setDeleteAccountId(account)
                .setTransferAccountId(operatorId)
                .setMaxTransactionFee(Hbar.fromTinybar(balance.asTinybar() / 2))
                .setTransactionId(TransactionId.generate(account))
                .build(client)
                .sign(key2)
                .execute(client);

            receipt = new TransactionReceiptQuery()
                .setTransactionId(transactionId)
                .execute(client);

            assertEquals(Status.Success, receipt.status);

            assertThrows(Exception.class, () -> {
                new AccountInfoQuery()
                    .setAccountId(account)
                    .execute(client);
            });
        });
    }
}
