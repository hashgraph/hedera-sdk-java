import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.ReceiptStatusException;
import com.hedera.hashgraph.sdk.TransactionReceipt;
import com.hedera.hashgraph.sdk.TransactionResponse;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public final class ValidateChecksumExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private ValidateChecksumExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        Scanner inputScanner = new Scanner(System.in);

        /*
         * Entity IDs, such as TokenId and AccountId, can be constructed from strings.
         * For example, the AccountId::fromString(String inString) method will attempt to parse
         * the input string and construct the expected AccountId object, and will throw an
         * IllegalArgumentException if the string is incorrectly formatted.
         *
         * From here on, I'll talk about methods on accountId, but equivalent methods exist
         * on every entity ID type.
         *
         * fromString() expects the input to look something like this: "1.2.3-asdfg".
         * Here, 1 is the shard, 2 is the realm, 3 is the number, and "asdfg" is the checksum.
         *
         * The checksum can be used to ensure that an entity ID is inputted correctly.
         * For example, if the string being parsed is from a config file, or from user input,
         * it could contain typos.
         *
         * You can use AccountId::getChecksum() to get the checksum of an accountId that was constructed
         * using fromString().  This will be the checksum from the input string.  fromString() will merely
         * parse the string and create an AccountId object with the expected shard, realm, num, and checksum
         * values.  fromString() will NOT verify that the AccountId maps to a valid account on the hedera
         * network, and it will not verify the checksum.
         *
         * To verify a checksum, call AccountId::validateChecksum(Client client).  If the checksum
         * is invalid, validateChecksum() will throw an InvalidChecksumException, otherwise it will return normally.
         *
         * The validity of a checksum depends on which network the client is connected to (EG mainnet or
         * testnet or previewnet).  For example, a checksum that is valid for a particular shard/realm/num
         * on mainnet will be INVALID for the same shard/realm/num on testnet.
         *
         * As far as fromString() is concerned, the checksum is optional.
         * If you use fromString() to generate an AccountId from a string that does not include a checksum,
         * such as "1.2.3", fromString() will work, but a call to the getChecksum() method on the resulting
         * account ID will return null.
         *
         * Generally speaking, AccountId objects can come from three places:
         * 1) AccountId.fromString(inString)
         * 2) new AccountId(shard, realm, num)
         * 3) From the result of a query
         *
         * In the first case, the AccountId object will have a checksum (getChecksum() will not return null) if
         * the input string included a checksum, and it will not have a checksum if the string did not
         * include a checksum.
         *
         * In the second and third cases, the AccountId object will not have a checksum.
         *
         * AccountId::toString() will print the string with no checksum, AccountId::toStringWithChecksum(Client client)
         * will print the string with a checksum.
         */


        // TODO
    }
}
