import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.ScheduleInfoQuery;
import com.hedera.hashgraph.sdk.ScheduleSignTransaction;
import com.hedera.hashgraph.sdk.Status;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionReceiptQuery;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ScheduleIdenticalTransactionExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ScheduleIdenticalTransactionExample() {
    }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException {

        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        System.out.println("threshold key example");
        System.out.println("Keys:");

        PrivateKey[] privKeys = new PrivateKey[3];
        PublicKey[] pubKeys = new PublicKey[3];
        Client[] clients = new Client[3];
        AccountId[] accounts = new AccountId[3];

        @Var
        ScheduleId scheduleID = null;

        // Loop to generate keys, clients, and accounts
        for (int i = 0; i < 3 ; i++) {
            PrivateKey newKey = PrivateKey.generateED25519();
            privKeys[i] = newKey;
            pubKeys[i] = newKey.getPublicKey();

            System.out.println("Key #" + i + ":");
            System.out.println("private = " + privKeys[i]);
            System.out.println("public = " + pubKeys[i]);

            TransactionResponse createResponse = new AccountCreateTransaction()
                .setKey(newKey)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            // Make sure the transaction succeeded
            TransactionReceipt transactionReceipt = createResponse.getReceipt(client);

            Client newClient = Client.forName(HEDERA_NETWORK);
            newClient.setOperator(Objects.requireNonNull(transactionReceipt.accountId), newKey);
            clients[i] = newClient;
            accounts[i] = transactionReceipt.accountId;

            System.out.println("account = " + accounts[i]);
        }   // Loop to generate keys, clients, and accounts

        // A threshold key with a threshold of 2 and length of 3 requires
        // at least 2 of the 3 keys to sign anything modifying the account
        KeyList keyList = KeyList.withThreshold(2);
        Collections.addAll(keyList, pubKeys);

        // We are using all of these keys, so the scheduled transaction doesn't automatically go through
        // It works perfectly fine with just one key
        TransactionResponse createResponse = new AccountCreateTransaction()
            // The key that must sign each transfer out of the account. If receiverSigRequired is true, then
            // it must also sign any transfer into the account.
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(client);

        // Make sure the transaction succeeded
        TransactionReceipt receipt = createResponse.getReceipt(client);

        AccountId thresholdAccount = receipt.accountId;
        System.out.println("threshold account = " + thresholdAccount);

        for (Client loopClient : clients) {
            AccountId operatorId = loopClient.getOperatorAccountId();

            // Each loopClient creates an identical transaction, sending 1 hbar to each of the created accounts,
            // sent from the threshold Account
            TransferTransaction tx = new TransferTransaction();
            for (AccountId account : accounts) {
                tx.addHbarTransfer(account, new Hbar(1));
            }
            tx.addHbarTransfer(Objects.requireNonNull(thresholdAccount), new Hbar(3).negated());

            ScheduleCreateTransaction scheduledTx = new ScheduleCreateTransaction()
                .setScheduledTransaction(tx);

            scheduledTx.setPayerAccountId(thresholdAccount);

            TransactionResponse response = scheduledTx.execute(loopClient);

            TransactionReceipt loopReceipt = new TransactionReceiptQuery()
                .setTransactionId(response.transactionId)
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .execute(loopClient);

            System.out.println("operator [" + operatorId + "]: scheduleID = " + loopReceipt.scheduleId);

            // Save the schedule ID, so that it can be asserted for each loopClient submission
            if (scheduleID == null) {
                scheduleID = loopReceipt.scheduleId;
            }

            if (!scheduleID.equals(Objects.requireNonNull(loopReceipt.scheduleId))) {
                System.out.println("invalid generated schedule id, expected " + scheduleID + ", got " + loopReceipt.scheduleId);
                return;
            }

            // If the status return by the receipt is related to already created, execute a schedule sign transaction
            if (loopReceipt.status == Status.IDENTICAL_SCHEDULE_ALREADY_CREATED) {
                TransactionResponse signTransaction = new ScheduleSignTransaction()
                    .setScheduleId(scheduleID)
                    .setNodeAccountIds(Collections.singletonList(createResponse.nodeId))
                    .setScheduleId(loopReceipt.scheduleId)
                    .execute(loopClient);

                TransactionReceipt signReceipt = new TransactionReceiptQuery()
                    .setTransactionId(signTransaction.transactionId)
                    .execute(client);
                if (signReceipt.status != Status.SUCCESS && signReceipt.status != Status.SCHEDULE_ALREADY_EXECUTED) {
                    System.out.println("Bad status while getting receipt of schedule sign with operator " + operatorId + ": " + signReceipt.status);
                    return;
                }
            }
        }

        System.out.println(new ScheduleInfoQuery().setScheduleId(scheduleID).execute(client));

        AccountDeleteTransaction thresholdDeleteTx = new AccountDeleteTransaction()
            .setAccountId(thresholdAccount)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client);

        for (int i = 0; i < 3; i++) {
            thresholdDeleteTx.sign(privKeys[i]);
            new AccountDeleteTransaction()
                .setAccountId(accounts[i])
                .setTransferAccountId(OPERATOR_ID)
                .freezeWith(client)
                .sign(privKeys[i])
                .execute(client)
                .getReceipt(client);
        }

        thresholdDeleteTx
            .execute(client)
            .getReceipt(client);
    }
}
