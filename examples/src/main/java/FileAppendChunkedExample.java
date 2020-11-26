import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.proto.FileAppend;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

public class FileAppendChunkedExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private FileAppendChunkedExample() { }

    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException {
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

        // get a large file to send
        String fileContents = readResources("large_message.txt");

        TransactionResponse transactionResponse = new FileCreateTransaction()
            // Use the same key as the operator to "own" this file
            .setKeys(OPERATOR_KEY.getPublicKey())
            .setContents("Hello from Hedera.")
            // The default max fee of 1 HBAR is not enough to make a file ( starts around 1.1 HBAR )
            .setMaxTransactionFee(new Hbar(2)) // 2 HBAR
            .execute(client);

        TransactionReceipt receipt = transactionResponse.getReceipt(client);
        FileId newFileId = Objects.requireNonNull(receipt.fileId);

        System.out.println("fileId: " + newFileId);

        StringBuilder contents = new StringBuilder();

        for (int i=0; i <= 4096*9; i++){
            contents.append("1");
        }

        TransactionReceipt fileAppendReceipt = new FileAppendTransaction()
            .setNodeAccountIds(Collections.singletonList(transactionResponse.nodeId))
            .setFileId(newFileId)
            .setContents(contents.toString())
            .setMaxTransactionFee(new Hbar(1000))
            .freezeWith(client)
            .execute(client)
            .getReceipt(client);

        System.out.println(fileAppendReceipt.toString());

        FileInfo info = new FileInfoQuery()
            .setFileId(newFileId)
            .execute(client);

        System.out.println("File size according to `FileInfoQuery`: " + info.size);
    }


    private static String readResources(String filename) {
        ClassLoader classLoader = ConsensusPubSubChunkedExample.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(filename);
        StringBuilder bigContents = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(inputStream), UTF_8))) {
            @Var String line;
            while ((line = reader.readLine()) != null) {
                bigContents.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return bigContents.toString();
    }
}
