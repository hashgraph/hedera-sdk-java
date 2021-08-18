import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.AccountBalance;
import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.ScheduleInfo;
import com.hedera.hashgraph.sdk.ScheduleInfoQuery;
import com.hedera.hashgraph.sdk.ScheduleSignTransaction;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class ScheduledTransferExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ScheduledTransferExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
        Objects.requireNonNull(client.getOperatorAccountId());

        /*
         * A scheduled transaction is a transaction that has been proposed by an account,
         * but which requires more signatures before it will actually execute on the Hedera network.
         *
         * For example, if Alice wants to transfer an amount of Hbar to Bob, and Bob has
         * receiverSignatureRequired set to true, then that transaction must be signed by
         * both Alice and Bob before the transaction will be executed.
         *
         * To solve this problem, Alice can propose the transaction by creating a scheduled
         * transaction on the Hedera network which, if executed, would transfer Hbar from
         * Alice to Bob.  That scheduled transaction will have a ScheduleId by which we can
         * refer to that scheduled transaction.  Alice can communicate the ScheduleId to Bob, and
         * then Bob can use a ScheduleSignTransaction to sign that scheduled transaction.
         *
         * Bob has a 30 minute window in which to sign the scheduled transaction, starting at the
         * moment that Alice creates the scheduled transaction.  If a scheduled transaction
         * is not signed by all of the necessary signatories within the 30 minute window,
         * that scheduled transaction will expire, and will not be executed.
         *
         * Once a scheduled transaction has all of the signatures necessary to execute, it will
         * be executed on the Hedera network automatically.  If you create a scheduled transaction
         * on the Hedera network, but that transaction only requires your signature in order to
         * execute and no one else's, that scheduled transaction will be automatically
         * executed immediately.
         */

        PrivateKey bobsKey = PrivateKey.generate();
        AccountId bobsId = new AccountCreateTransaction()
            .setReceiverSignatureRequired(true)
            .setKey(bobsKey)
            .setInitialBalance(new Hbar(10))
            .freezeWith(client)
            .sign(bobsKey)
            .execute(client)
            .getReceipt(client)
            .accountId;
        Objects.requireNonNull(bobsId);

        System.out.println("Alice's ID: " + client.getOperatorAccountId().toStringWithChecksum(client));
        System.out.println("Bob's ID: " + bobsId.toStringWithChecksum(client));

        AccountBalance bobsInitialBalance = new AccountBalanceQuery()
            .setAccountId(bobsId)
            .execute(client);
        System.out.println("Bob's initial balance:");
        System.out.println(bobsInitialBalance);

        TransferTransaction transferToSchedule = new TransferTransaction()
            .addHbarTransfer(client.getOperatorAccountId(), new Hbar(-10))
            .addHbarTransfer(bobsId, new Hbar(10));
        System.out.println("Transfer to be scheduled:");
        System.out.println(transferToSchedule);

        /*
         * The payerAccountId is the account that will be charged the fee
         * for executing the scheduled transaction if/when it is executed.
         * That fee is separate from the fee that we will pay to execute the
         * ScheduleCreateTransaction itself.
         *
         * To clarify: Alice pays a fee to execute the ScheduleCreateTransaction,
         * which creates the scheduled transaction on the Hedera network.
         * She specifies when creating the scheduled transaction that Bob will pay
         * the fee for the scheduled transaction when it is executed.
         *
         * If payerAccountId is not specified, the account who creates the scheduled transaction
         * will be charged for executing the scheduled transaction.
         */

        ScheduleId scheduleId = new ScheduleCreateTransaction()
            .setScheduledTransaction(transferToSchedule)
            .setPayerAccountId(bobsId)
            .execute(client)
            .getReceipt(client)
            .scheduleId;
        Objects.requireNonNull(scheduleId);
        System.out.println("The scheduleId is: " + scheduleId.toStringWithChecksum(client));

        /*
         * Bob's balance should be unchanged.  The transfer has been scheduled, but it hasn't been executed yet
         * because it requires Bob's signature.
         */
        AccountBalance bobsBalanceAfterSchedule = new AccountBalanceQuery()
            .setAccountId(bobsId)
            .execute(client);
        System.out.println("Bob's balance after scheduling the transfer (should be unchanged):");
        System.out.println(bobsBalanceAfterSchedule);

        /*
         * Once Alice has communicated the scheduleId to Bob, Bob can query for information about the
         * scheduled transaction.
         */
        ScheduleInfo scheduledTransactionInfo = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
        System.out.println("Info about scheduled transaction:");
        System.out.println(scheduledTransactionInfo);

        /*
         * getScheduledTransaction() will return an SDK Transaction object identical to the transaction
         * that was scheduled, which Bob can then inspect like a normal transaction.
         */
        try {
            // Throws com.google.protobuf.InvalidProtocolBufferException
            Transaction<?> scheduledTransaction = scheduledTransactionInfo.getScheduledTransaction();

            // We happen to know that this transaction is (or certainly ought to be) a TransferTransaction
            if (scheduledTransaction instanceof TransferTransaction) {
                TransferTransaction scheduledTransfer = (TransferTransaction) scheduledTransaction;
                System.out.println("The scheduled transfer transaction from Bob's POV:");
                System.out.println(scheduledTransfer);
            } else {
                System.out.println("The scheduled transaction was not a transfer transaction.");
                System.out.println("Something has gone horribly wrong.  Crashing...");
                System.exit(-1);
            }
        } catch (InvalidProtocolBufferException exc) {
            System.out.println("Failed to get copy of scheduled transaction, crashing...");
            System.exit(-1);
        }

        TransactionReceipt signReceipt = new ScheduleSignTransaction()
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(bobsKey)
            .execute(client)
            .getReceipt(client);

        AccountBalance balanceAfterSigning = new AccountBalanceQuery()
            .setAccountId(bobsId)
            .execute(client);
        System.out.println("Bob's balance after signing the scheduled transaction:");
        System.out.println(balanceAfterSigning);

        ScheduleInfo postTransactionInfo = new ScheduleInfoQuery()
            .setScheduleId(scheduleId)
            .execute(client);
        System.out.println("Info on the scheduled transaction, executedAt should no longer be null:");
        System.out.println(postTransactionInfo);

        // Clean up
        new AccountDeleteTransaction()
            .setTransferAccountId(client.getOperatorAccountId())
            .setAccountId(bobsId)
            .freezeWith(client)
            .sign(bobsKey)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
