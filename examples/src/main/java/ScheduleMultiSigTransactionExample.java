import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ScheduleCreateTransaction;
import com.hedera.hashgraph.sdk.ScheduleId;
import com.hedera.hashgraph.sdk.ScheduleInfo;
import com.hedera.hashgraph.sdk.ScheduleInfoQuery;
import com.hedera.hashgraph.sdk.ScheduleSignTransaction;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import com.hedera.hashgraph.sdk.TransferTransaction;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class ScheduleMultiSigTransactionExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    ScheduleMultiSigTransactionExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        AccountId operatorId = Objects.requireNonNull(client.getOperatorAccountId());

        // Generate 3 random keys
        PrivateKey key1 = PrivateKey.generateED25519();
        PrivateKey key2 = PrivateKey.generateED25519();
        PrivateKey key3 = PrivateKey.generateED25519();

        // Create a keylist from those keys. This key will be used as the new account's key
        // The reason we want to use a `KeyList` is to simulate a multi-party system where
        // multiple keys are required to sign.
        KeyList keyList = new KeyList();

        keyList.add(key1.getPublicKey());
        keyList.add(key2.getPublicKey());
        keyList.add(key3.getPublicKey());

        System.out.println("key1 private = " + key1);
        System.out.println("key1 public = " + key1.getPublicKey());
        System.out.println("key1 private = " + key2);
        System.out.println("key2 public = " + key2.getPublicKey());
        System.out.println("key1 private = " + key3);
        System.out.println("key3 public = " + key3.getPublicKey());
        System.out.println("keyList = " + keyList);

        // Creat the account with the `KeyList`
        TransactionResponse response = new AccountCreateTransaction()
            .setNodeAccountIds(Collections.singletonList(new AccountId(3)))
            // The only _required_ property here is `key`
            .setKey(keyList)
            .setInitialBalance(new Hbar(10))
            .execute(client);

        // This will wait for the receipt to become available
        @Var TransactionReceipt receipt = response.getReceipt(client);

        AccountId accountId = Objects.requireNonNull(receipt.accountId);

        System.out.println("accountId = " + accountId);

        // Generate a `TransactionId`. This id is used to query the inner scheduled transaction
        // after we expect it to have been executed
        TransactionId transactionId = TransactionId.generate(operatorId);

        System.out.println("transactionId for scheduled transaction = " + transactionId);

        // Create a transfer transaction with 2/3 signatures.
        @Var TransferTransaction transfer = new TransferTransaction()
            .addHbarTransfer(accountId, new Hbar(1).negated())
            .addHbarTransfer(operatorId, new Hbar(1));

        // Schedule the transactoin
        ScheduleCreateTransaction scheduled = transfer.schedule()
            .setPayerAccountId(client.getOperatorAccountId())
            .setAdminKey(client.getOperatorPublicKey())
            .freezeWith(client)
            .sign(key2);

        receipt = scheduled.execute(client).getReceipt(client);

        // Get the schedule ID from the receipt
        ScheduleId scheduleId = Objects.requireNonNull(receipt.scheduleId);

        System.out.println("scheduleId = " + scheduleId);

        // Get the schedule info to see if `signatories` is populated with 2/3 signatures
        ScheduleInfo info = new ScheduleInfoQuery()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .execute(client);

        System.out.println("Schedule Info = " + info);

        transfer = (TransferTransaction) info.getScheduledTransaction();

        Map<AccountId, Hbar> transfers = transfer.getHbarTransfers();

        // Make sure the transfer transaction is what we expect
        if (transfers.size() != 2) {
            throw new Exception("more transfers than expected");
        }

        if (!transfers.get(accountId).equals(new Hbar(1).negated())) {
            throw new Exception("transfer for " + accountId + " is not what is expected " + transfers.get(accountId));
        }

        if (!transfers.get(operatorId).equals(new Hbar(1))) {
            throw new Exception("transfer for " + operatorId + " is not what is expected " + transfers.get(operatorId));
        }

        System.out.println("sending schedule sign transaction");

        // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
        // since all 3 signatures have been provided.
        new ScheduleSignTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .freezeWith(client)
            .sign(key3)
            .execute(client)
            .getReceipt(client);

        // Query the schedule info again
        new ScheduleInfoQuery()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .execute(client);
    }
}
