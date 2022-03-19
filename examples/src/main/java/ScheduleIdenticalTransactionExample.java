import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

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

        PublicKey[] pubKeys = new PublicKey[3];
        Client[] clients = new Client[3];
        AccountId[] accounts = new AccountId[3];

//        ScheduleId scheduleID = new ScheduleId();

        // Loop to generate keys, clients, and accounts
        for (int i = 0; i < 3 ; i++) {
            PrivateKey newKey = PrivateKey.generateED25519();
            pubKeys[i] = newKey.getPublicKey();

            System.out.println("Key #" + i + ":");
            System.out.println("private = " + newKey);
            System.out.println("public = " + pubKeys[i]);

            TransactionResponse createResponse = new AccountCreateTransaction()
                .setKey(newKey)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            // Make sure the transaction succeeded
            TransactionReceipt transactionReceipt = createResponse.getReceipt(client);

            Client newClient = Client.forName(HEDERA_NETWORK);
            newClient.setOperator(transactionReceipt.accountId, newKey);
            clients[i] = newClient;
            accounts[i] = transactionReceipt.accountId;

            System.out.println("account = " + accounts[i]);
        }



    }
}
