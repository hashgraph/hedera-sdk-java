import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ScheduledTransactionMultiSigThresholdExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ScheduledTransactionMultiSigThresholdExample() {
    }

//    public static void main(String[] args) throws PrecheckStatusException, IOException, TimeoutException, ReceiptStatusException {
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);


        // Generate four new Ed25519 private, public key pairs.
        PrivateKey[] privateKeys = new PrivateKey[4];
        PublicKey[] publicKeys = new PublicKey[4];
        for (int i = 0; i < 4; i++) {
            PrivateKey key = PrivateKey.generateED25519();
            privateKeys[i] = key;
            publicKeys[i] = key.getPublicKey();
            System.out.println("public key " + (i + 1) + ": " + publicKeys[i]);
            System.out.println("private key " + (i + 1) + ": " + privateKeys[i]);
        }

        // require 3 of the 4 keys we generated to sign on anything modifying this account
        KeyList transactionKey = KeyList.withThreshold(3);
        Collections.addAll(transactionKey, publicKeys);

        TransactionResponse transactionResponse = new AccountCreateTransaction()
            .setKey(transactionKey)
            .setInitialBalance(Hbar.fromTinybars(1))
            .setAccountMemo("3-of-4 multi-sig account")
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt txAccountCreateReceipt = transactionResponse.getReceipt(client);
        AccountId multiSigAccountId = txAccountCreateReceipt.accountId;
        System.out.println("3-of-4 multi-sig account ID: " + multiSigAccountId);

        AccountBalance balance = new AccountBalanceQuery()
            .setAccountId(multiSigAccountId)
            .execute(client);
        System.out.println("Balance of account " + multiSigAccountId + ": " + balance.hbars.toTinybars() + " tinybar.");

        // schedule crypto transfer from multi-sig account to operator account
        TransactionResponse transferToSchedule = new TransferTransaction()
            .addHbarTransfer(multiSigAccountId, Hbar.fromTinybars(-1))
            .addHbarTransfer(client.getOperatorAccountId(), Hbar.fromTinybars(1))
            .schedule()
            .freezeWith(client)
            .sign(privateKeys[0])   // add 1 signature`
            .execute(client);

        TransactionReceipt txScheduleReceipt = transferToSchedule.getReceipt(client);
        System.out.println("Schedule status: " + txScheduleReceipt.status);
        ScheduleId scheduleId = txScheduleReceipt.scheduleId;
        System.out.println("Schedule ID: " + scheduleId);
        TransactionId scheduledTxId = txScheduleReceipt.scheduledTransactionId;
        System.out.println("Scheduled tx ID: " + scheduledTxId);

        // add 2 signature
        TransactionResponse txScheduleSign1 = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(privateKeys[1])
            .execute(client);

        TransactionReceipt txScheduleSign1Receipt = txScheduleSign1.getReceipt(client);
        System.out.println("1. ScheduleSignTransaction status: " + txScheduleSign1Receipt.status);

        // add 3 signature
        TransactionResponse txScheduleSign2 = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(privateKeys[2])
            .execute(client);

        TransactionReceipt txScheduleSign2Receipt = txScheduleSign2.getReceipt(client);
        System.out.println("2. ScheduleSignTransaction status: " + txScheduleSign2Receipt.status);

        // query schedule
        ScheduleInfo scheduleInfo = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
        System.out.println(scheduleInfo);

        // query triggered scheduled tx
        TransactionRecord recordScheduledTx = new TransactionRecordQuery()
            .setTransactionId(scheduledTxId)
            .execute(client);
        System.out.println(recordScheduledTx);

    }

}
