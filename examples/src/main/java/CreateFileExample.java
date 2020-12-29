import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.PrecheckStatusException;

import io.github.cdimascio.dotenv.Dotenv;

public final class CreateFileExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private CreateFileExample() { }

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

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        String fileContents = "Hedera hashgraph is great!";

        TransactionResponse transactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .setKeys(OPERATOR_KEY.getPublicKey())
            .setContents(fileContents)
            // The default max fee of 1 HBAR is not enough to make a file ( starts around 1.1 HBAR )
            .setMaxTransactionFee(new Hbar(2)) // 2 HBAR
            .execute(client);

        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        FileId newFileId = receipt.fileId;

        System.out.println("file: " + newFileId);
    }
}
