import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class ScheduleMultiSigTransactionExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    ScheduleMultiSigTransactionExample() {
    }

    public static Client createClient() {
        @Var Client client;

        if (HEDERA_NETWORK != null && HEDERA_NETWORK.equals("previewnet")) {
            client = Client.forPreviewnet();
        } else {
            try {
                client = Client.fromConfigFile(CONFIG_FILE != null ? CONFIG_FILE : "");
            } catch (Exception e) {
                client = Client.forTestnet();
            }
        }

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        return client;
    }

    public static void main(String[] args) throws Exception {
        Client client = createClient();
        AccountId operatorId = Objects.requireNonNull(client.getOperatorAccountId());

        // Generate 3 random keys
        PrivateKey key1 = PrivateKey.generate();
        PrivateKey key2 = PrivateKey.generate();
        PrivateKey key3 = PrivateKey.generate();

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
        TransferTransaction transfer = new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTransactionId(transactionId)
            .addHbarTransfer(accountId, new Hbar(1).negated())
            .addHbarTransfer(operatorId, new Hbar(1))
            .freezeWith(client)
            .sign(key1);

        // Schedule the transactoin
        ScheduleCreateTransaction scheduled = transfer.schedule();

        byte[] key2Signature = key2.signTransaction(transfer);

        scheduled.addScheduleSignature(key2.getPublicKey(), key2Signature);

        if (scheduled.getScheduleSignatures().size() != 2) {
            throw new Exception("Scheduled transaction has incorrect number of signatures: " + scheduled.getScheduleSignatures().size());
        }

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

        transfer = (TransferTransaction) info.getTransaction();

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

        // Get the last signature for the inner scheduled transaction
        byte[] key3Signature = key3.signTransaction(transfer);

        System.out.println("sending schedule sign transaction");

        // Finally send this last signature to Hedera. This last signature _should_ mean the transaction executes
        // since all 3 signatures have been provided.
        ScheduleSignTransaction signTransaction = new ScheduleSignTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setScheduleId(scheduleId)
            .addScheduleSignature(key3.getPublicKey(), key3Signature);

        if (signTransaction.getScheduleSignatures().size() != 1) {
            throw new Exception("Scheduled sign transaction has incorrect number of signatures: " + signTransaction.getScheduleSignatures().size());
        }

        signTransaction.execute(client).getReceipt(client);

        // Query the schedule info again
        try {
            new ScheduleInfoQuery()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setScheduleId(scheduleId)
                .execute(client);
        } catch (PrecheckStatusException e) {
            System.out.println("Received " + e.status + " status code which implies scheduled transaction was executed");
        }

//        Cannot seem to get the receipt for a scheduled transaction after it's expected to have executed
//        receipt = new TransactionReceiptQuery()
//            .setNodeAccountIds(Collections.singletonList(response.nodeId))
//            .setTransactionId(transactionId)
//            .execute(client);
//
//        System.out.println("receipt = " + receipt);
    }
}
