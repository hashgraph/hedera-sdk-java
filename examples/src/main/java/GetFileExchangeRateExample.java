import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ExchangeRateSet;
import com.hedera.hashgraph.sdk.FileContentsQuery;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class GetFileExchangeRateExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private GetFileExchangeRateExample() {
    }

    public static void main(String[] args) throws ReceiptStatusException, TimeoutException, PrecheckStatusException, InvalidProtocolBufferException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Get contents of file 0.0.112
        ByteString fileContentsByteString = new FileContentsQuery()
            .setFileId(FileId.fromString("0.0.112"))
            .execute(client);

        System.out.println(Hex.decode());

        byte[] fileContents = fileContentsByteString.toByteArray();

        ExchangeRateSet exchangeRateSet = ExchangeRateSet.fromBytes(fileContents);

        // Prints query results to console
        System.out.println("Current: " + exchangeRateSet.currentRate.hbars);
        System.out.println("Current: " + exchangeRateSet.currentRate.cents);
        System.out.println("Current: " + exchangeRateSet.currentRate.expirationTime.toString());

        System.out.println("Next: " + exchangeRateSet.nextRate.hbars);
        System.out.println("Next: " + exchangeRateSet.nextRate.cents);
        System.out.println("Next: " + exchangeRateSet.nextRate.expirationTime.toString());
    }
}
