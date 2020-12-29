import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class CreateAccountExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private CreateAccountExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
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

        // Generate a Ed25519 private, public key pair
        PrivateKey newKey = PrivateKey.generate();
        PublicKey newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        TransactionResponse transactionResponse = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newPublicKey)
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionResponse.getReceipt(client);

        AccountId newAccountId = receipt.accountId;

        System.out.println("account = " + newAccountId);
    }
}
