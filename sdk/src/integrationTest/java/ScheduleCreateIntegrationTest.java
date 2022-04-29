import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.ScheduleInfo;
import com.hedera.hashgraph.sdk.ScheduleInfoQuery;
import com.hedera.hashgraph.sdk.ScheduleSignTransaction;
import com.hedera.hashgraph.sdk.TokenAssociateTransaction;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TopicCreateTransaction;
import com.hedera.hashgraph.sdk.TopicMessageSubmitTransaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ScheduleCreateIntegrationTest {
    @Test
    @Disabled
    @DisplayName("Can create schedule")
    void canCreateSchedule() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(10));

        var response = new ScheduleCreateTransaction()
            .setScheduledTransaction(transaction)
            .setAdminKey(testEnv.operatorKey)
            .setPayerAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

        var info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info.executedAt).isNotNull();

        testEnv.close();
    }

    @Test
    @Disabled
    @DisplayName("Can get Transaction")
    void canGetTransactionSchedule() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(10));

        var response = new ScheduleCreateTransaction()
            .setScheduledTransaction(transaction)
            .setAdminKey(testEnv.operatorKey)
            .setPayerAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

        var info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info.executedAt).isNotNull();
        assertThat(info.getScheduledTransaction()).isNotNull();

        testEnv.close();
    }

    @Test
    @Disabled
    @DisplayName("Can create schedule with schedule()")
    void canCreateWithSchedule() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var transaction = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(10));

        var tx = transaction.schedule();

        var response = tx
            .setAdminKey(testEnv.operatorKey)
            .setPayerAccountId(testEnv.operatorId)
            .execute(testEnv.client);

        var scheduleId = Objects.requireNonNull(response.getReceipt(testEnv.client).scheduleId);

        var info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info.executedAt).isNotNull();
        assertThat(info.getScheduledTransaction()).isNotNull();

        testEnv.close();
    }

    @Test
    @DisplayName("Can sign schedule")
    void canSignSchedule2() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();
        PrivateKey key3 = PrivateKey.generateED25519();

        KeyList keyList = new KeyList();

        keyList.add(key1.getPublicKey());
        keyList.add(key2.getPublicKey());
        keyList.add(key3.getPublicKey());

        // Creat the account with the `KeyList`
        TransactionResponse response = new AccountCreateTransaction()
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(testEnv.client);

        // This will wait for the receipt to become available
        @Var TransactionReceipt receipt = response.getReceipt(testEnv.client);

        AccountId accountId = Objects.requireNonNull(receipt.accountId);

        // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
        // after we expect it to have been executed
        TransactionId transactionId = TransactionId.generate(testEnv.operatorId);

        // Create a transfer transaction with 2/3 signatures.
        TransferTransaction transfer = new TransferTransaction()
            .setTransactionId(transactionId)
            .addHbarTransfer(accountId, new Hbar(1).negated())
            .addHbarTransfer(testEnv.operatorId, new Hbar(1));

        // Schedule the transactoin
        ScheduleCreateTransaction scheduled = transfer.schedule();

        receipt = scheduled.execute(testEnv.client).getReceipt(testEnv.client);

        // Get the schedule ID from the receipt
        ScheduleId scheduleId = Objects.requireNonNull(receipt.scheduleId);

        // Get the schedule info to see if `signatories` is populated with 2/3 signatures
        @Var ScheduleInfo info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info.executedAt).isNull();

        // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
        // since all 3 signatures have been provided.
        ScheduleSignTransaction signTransaction = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(testEnv.client);

        signTransaction.sign(key1).sign(key2).sign(key3).execute(testEnv.client).getReceipt(testEnv.client);

        info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info.executedAt).isNotNull();

        new AccountDeleteTransaction()
            .setAccountId(accountId)
            .setTransferAccountId(testEnv.operatorId)
            .freezeWith(testEnv.client)
            .sign(key1).sign(key2).sign(key3)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        testEnv.close();
    }

    @Test
    @DisplayName("Can schedule token transfer")
    void canScheduleTokenTransfer() throws Exception {
        var testEnv = new IntegrationTestEnv(1).useThrowawayAccount();

        PrivateKey key = PrivateKey.generateED25519();

        var accountId = new AccountCreateTransaction()
            .setReceiverSignatureRequired(true)
            .setKey(key)
            .setInitialBalance(new Hbar(10))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        Objects.requireNonNull(accountId);

        var tokenId = new TokenCreateTransaction()
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setInitialSupply(100)
            .setTreasuryAccountId(testEnv.operatorId)
            .setAdminKey(testEnv.operatorKey)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .tokenId;

        Objects.requireNonNull(tokenId);

        new TokenAssociateTransaction()
            .setAccountId(accountId)
            .setTokenIds(Collections.singletonList(tokenId))
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var scheduleId = new TransferTransaction()
            .addTokenTransfer(tokenId, testEnv.operatorId, -10)
            .addTokenTransfer(tokenId, accountId, 10)
            .schedule()
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .scheduleId;

        Objects.requireNonNull(scheduleId);

        var balanceQuery1 = new AccountBalanceQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(balanceQuery1.tokens.get(tokenId)).isEqualTo(0);

        new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(testEnv.client)
            .sign(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        var balanceQuery2 = new AccountBalanceQuery()
            .setAccountId(accountId)
            .execute(testEnv.client);

        assertThat(balanceQuery2.tokens.get(tokenId)).isEqualTo(10);

        testEnv.close(tokenId, accountId, key);
    }

    @Test
    @DisplayName("Cannot schedule two identical transactions")
    void cannotScheduleTwoTransactions() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();
        var accountId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(10))
            .setKey(key)
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .accountId;

        var transferTx = new TransferTransaction()
            .addHbarTransfer(testEnv.operatorId, new Hbar(-10))
            .addHbarTransfer(accountId, new Hbar(10));

        var scheduleId1 = transferTx.schedule()
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .scheduleId;

        var info1 = new ScheduleInfoQuery()
            .setScheduleId(scheduleId1)
            .execute(testEnv.client);

        assertThat(info1.executedAt).isNotNull();

        var transferTxFromInfo = info1.getScheduledTransaction();

        var scheduleCreateTx1 = transferTx.schedule();
        var scheduleCreateTx2 = transferTxFromInfo.schedule();

        assertThat(scheduleCreateTx2.toString()).isEqualTo(scheduleCreateTx1.toString());

        assertThatExceptionOfType(ReceiptStatusException.class).isThrownBy(() -> {
            transferTxFromInfo.schedule()
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining("IDENTICAL_SCHEDULE_ALREADY_CREATED");

        testEnv.close(accountId, key);
    }

    @Test
    @DisplayName("Can schedule topic message")
    void canScheduleTopicMessage() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        // Generate 3 random keys
        var key1 = PrivateKey.generateED25519();

        // This is the submit key
        var key2 = PrivateKey.generateED25519();

        var key3 = PrivateKey.generateED25519();

        var keyList = new KeyList();

        keyList.add(key1.getPublicKey());
        keyList.add(key2.getPublicKey());
        keyList.add(key3.getPublicKey());

        var response = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(100))
            .setKey(keyList)
            .execute(testEnv.client);

        assertThat(response.getReceipt(testEnv.client).accountId).isNotNull();

        var topicId = Objects.requireNonNull(new TopicCreateTransaction()
            .setAdminKey(testEnv.operatorKey)
            .setAutoRenewAccountId(testEnv.operatorId)
            .setTopicMemo("HCS Topic_")
            .setSubmitKey(key2.getPublicKey())
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .topicId
        );

        var transaction = new TopicMessageSubmitTransaction()
            .setTopicId(topicId)
            .setMessage("scheduled hcs message".getBytes(StandardCharsets.UTF_8));

        // create schedule
        var scheduledTx = transaction.schedule()
            .setAdminKey(testEnv.operatorKey)
            .setPayerAccountId(testEnv.operatorId)
            .setScheduleMemo("mirror scheduled E2E signature on create and sign_" + Instant.now());

        var scheduled = scheduledTx.freezeWith(testEnv.client);

        var scheduleId = Objects.requireNonNull(scheduled
            .execute(testEnv.client)
            .getReceipt(testEnv.client)
            .scheduleId
        );

        // verify schedule has been created and has 1 of 2 signatures
        @Var var info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info).isNotNull();
        assertThat(info.scheduleId).isEqualTo(scheduleId);

        var infoTransaction = (TopicMessageSubmitTransaction) info.getScheduledTransaction();

        assertThat(transaction.getTopicId()).isEqualTo(infoTransaction.getTopicId());
        assertThat(transaction.getNodeAccountIds()).isEqualTo(infoTransaction.getNodeAccountIds());

        var scheduleSign = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(testEnv.client);

        scheduleSign
            .sign(key2)
            .execute(testEnv.client)
            .getReceipt(testEnv.client);

        info = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(testEnv.client);

        assertThat(info.executedAt).isNotNull();

        testEnv.close();
    }
}
