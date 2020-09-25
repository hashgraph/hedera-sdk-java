import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.proto.FileAppend;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class FileAppendExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private FileAppendExample() { }

    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException, IOException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // The file is required to be a byte array,
        // you can easily use the bytes of a file instead.
        byte[] fileContents = "Hedera hashgraph is great!".getBytes(StandardCharsets.UTF_8);

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

        String contents = "";

        for (int i=0; i <= 4096*9; i++){
            contents += "1";
        }

        TransactionResponse fileAppendResponse = new FileAppendTransaction()
            .setNodeId(new AccountId(3))
            .setFileId(newFileId)
            .setContents(contents)
            .setMaxTransactionFee(new Hbar(1000))
            .execute(client);
    }
}

// .setNodeId(AccountId.fromString("0.0.5005"))
//            .setTransactionId(new TransactionId(AccountId.fromString("0.0.5006"), validStart))
//            .setFileId(FileId.fromString("0.0.6006"))
//            .setContents(new byte[]{1, 2, 3, 4})
//            .setMaxTransactionFee(Hbar.fromTinybars(100_000))
//            .freeze()
//            .sign(unusedPrivateKey)
//            .toString()
