import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountBalanceQuery;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;

import io.github.cdimascio.dotenv.Dotenv;

public final class GetAccountBalanceExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private GetAccountBalanceExample() { }

    public static void main(String[] args) throws HederaPreCheckStatusException, TimeoutException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        Hbar balance = new AccountBalanceQuery()
            .setAccountId(OPERATOR_ID)
            .execute(client)
            .hbars;

        System.out.println("balance = " + balance);
    }
}
