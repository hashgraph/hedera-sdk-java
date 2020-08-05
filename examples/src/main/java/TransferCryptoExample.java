import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.CryptoTransferTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionRecord;

import io.github.cdimascio.dotenv.Dotenv;

public final class TransferCryptoExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private TransferCryptoExample() {
    }

    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        AccountId recipientId = AccountId.fromString("0.0.3");
        Hbar amount = Hbar.fromTinybars(10_000);

        Hbar senderBalanceBefore = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client);

        Hbar receiptBalanceBefore = new AccountBalanceQuery()
            .setAccountId(recipientId)
            .execute(client);

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceBefore);
        System.out.println("" + recipientId + " balance = " + receiptBalanceBefore);

        var transactionResponse = new CryptoTransferTransaction()
            // .addSender and .addRecipient can be called as many times as you want as long as the total sum from
            // both sides is equivalent
            .addSender(OPERATOR_ID, amount)
            .addRecipient(recipientId, amount)
            .setTransactionMemo("transfer test")
            .execute(client);

        System.out.println("transaction ID: " + transactionResponse);

        TransactionRecord record = transactionResponse.transactionId.getRecord(client);

        System.out.println("transferred " + amount + "...");

        Hbar senderBalanceAfter = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client);

        Hbar receiptBalanceAfter = new AccountBalanceQuery()
            .setAccountId(recipientId)
            .execute(client);

        System.out.println("" + OPERATOR_ID + " balance = " + senderBalanceAfter);
        System.out.println("" + recipientId + " balance = " + receiptBalanceAfter);
        System.out.println("Transfer memo: " + record.transactionMemo);
    }
}
