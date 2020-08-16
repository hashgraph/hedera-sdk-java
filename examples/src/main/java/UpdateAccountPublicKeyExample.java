import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfo;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.AccountUpdateTransaction;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TransactionResponse;

import io.github.cdimascio.dotenv.Dotenv;

public final class UpdateAccountPublicKeyExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private UpdateAccountPublicKeyExample() {
    }

    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException {
        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        client.setMaxTransactionFee(new Hbar(10));

        // First, we create a new account so we don't affect our account

        PrivateKey key1 = PrivateKey.generate();
        PrivateKey key2 = PrivateKey.generate();

        TransactionResponse acctTransactionResponse = new AccountCreateTransaction()
            .setKey(key1.getPublicKey())
            .setInitialBalance(new Hbar(1))
            .execute(client);

        System.out.println("transaction ID: " + acctTransactionResponse);
        AccountId accountId = Objects.requireNonNull(acctTransactionResponse.getReceipt(client).accountId);
        System.out.println("account = " + accountId);
        System.out.println("key = " + key1.getPublicKey());
        // Next, we update the key

        System.out.println(" :: update public key of account " + accountId);
        System.out.println("set key = " + key2.getPublicKey());

        var accountUpdateTransactionResponse = new AccountUpdateTransaction()
            .setAccountId(accountId)
            .setKey(key2.getPublicKey())
            .freezeWith(client)
            // sign with the previous key and the new key
            .sign(key1)
            .sign(key2)
            // execute will implicitly sign with the operator
            .execute(client);

        System.out.println("transaction ID: " + accountUpdateTransactionResponse);

        // (important!) wait for the transaction to complete by querying the receipt
        accountUpdateTransactionResponse.getReceipt(client);

        // Now we fetch the account information to check if the key was changed
        System.out.println(" :: getAccount and check our current key");

        AccountInfo info = new AccountInfoQuery()
            .setAccountId(accountId)
            .execute(client);

        System.out.println("key = " + info.key);
    }
}
