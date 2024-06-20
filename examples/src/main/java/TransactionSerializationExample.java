import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Transaction;
import com.hedera.hashgraph.sdk.TransactionRecord;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;

public class TransactionSerializationExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public TransactionSerializationExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        AccountId recipientId = AccountId.fromString("0.0.3");
        Hbar amount = Hbar.fromTinybars(10_000);

        Hbar senderBalanceBefore = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client)
            .hbars;

        Hbar receiptBalanceBefore = new AccountBalanceQuery()
            .setAccountId(recipientId)
            .execute(client)
            .hbars;

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        var transferTransaction = new TransferTransaction()
            // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
            // both sides is equivalent
            .addHbarTransfer(OPERATOR_ID, amount.negated());

        var transactionBytes = transferTransaction.toBytes();

        TransferTransaction transferTransactionDeserialized = (TransferTransaction) Transaction.fromBytes(transactionBytes);

        var transactionResponse = transferTransactionDeserialized
            .addHbarTransfer(recipientId, amount)
            .setTransactionMemo("transfer test")
            .execute(client);

        System.out.println("transaction ID: " + transactionResponse);

        TransactionRecord record = transactionResponse.getRecord(client);

        System.out.println("transferred " + amount + "...");

        Hbar senderBalanceAfter = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client)
            .hbars;

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
            .setAccountId(recipientId)
            .execute(client)
            .hbars;

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.transactionMemo);
    }
}
