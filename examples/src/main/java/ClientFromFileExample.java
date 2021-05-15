import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;

import io.github.cdimascio.dotenv.Dotenv;

public final class ClientFromFileExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private ClientFromFileExample() { }

    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, IOException, ReceiptStatusException {
        Client client;

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

        // default max fee for all transactions executed by this client
        client.setMaxTransactionFee(new Hbar(100));
        client.setMaxQueryPayment(new Hbar(10));

        PrivateKey newKey = PrivateKey.generate();
        PublicKey newPublicKey = newKey.getPublicKey();

        TransactionResponse transactionResponse = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionResponse.getReceipt(client);

        AccountId newAccountId = receipt.accountId;

        System.out.println("account 1 = " + newAccountId);

        InputStream clientConfigWithOperator = ClientFromFileExample.class.getClassLoader()
            .getResourceAsStream("client-config-with-operator.json");

        if(clientConfigWithOperator == null){
            throw new Error("Error reading client-config-with-operator.json");
        }

        try{
            client = Client.fromConfig(new InputStreamReader(clientConfigWithOperator, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new Error("Client from config error: " + e.getMessage());
        }

        transactionResponse = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        receipt = transactionResponse.getReceipt(client);

        newAccountId = receipt.accountId;

        System.out.println("account 2 = " + newAccountId);

        InputStream clientConfig = ClientFromFileExample.class.getClassLoader()
            .getResourceAsStream("client-config.json");

        if(clientConfig == null){
            throw new Error("Error reading client-config.json");
        }

        try{
            client = Client.fromConfig(new InputStreamReader(clientConfig, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new Error("Client from config error: " + e.getMessage());
        }

        transactionResponse = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        receipt = transactionResponse.getReceipt(client);

        newAccountId = receipt.accountId;

        System.out.println("account 3 = " + newAccountId);
    }
}
