import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.*;
import com.hedera.hashgraph.sdk.PrecheckStatusException;

import com.google.protobuf.ByteString;
import io.github.cdimascio.dotenv.Dotenv;

/** Get the network address book for inspecting the node public keys, among other things */
public final class GetAddressBookExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");

    private GetAddressBookExample() { }

    public static void main(String[] args) throws PrecheckStatusException, IOException, TimeoutException {
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

        FileContentsQuery fileQuery = new FileContentsQuery()
            .setFileId(FileId.ADDRESS_BOOK);

        Hbar cost = fileQuery.getCost(client);
        System.out.println("file contents cost: " + cost);

        fileQuery.setMaxQueryPayment(new Hbar(1));

        ByteString contents = fileQuery.execute(client);

        Files.deleteIfExists(FileSystems.getDefault().getPath("address-book.proto.bin"));

        Files.copy(new ByteArrayInputStream(contents.toByteArray()),
            FileSystems.getDefault().getPath("address-book.proto.bin"));
    }
}
