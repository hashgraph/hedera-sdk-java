import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.AccountDeleteTransaction;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.AccountInfoQuery;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.HederaReceiptStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.TransactionReceipt;

import io.github.cdimascio.dotenv.Dotenv;

public final class DeleteAccountExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private DeleteAccountExample() { }

    public static void main(String[] args) throws TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException {
        // Generate a Ed25519 private, public key pair
        PrivateKey newKey = PrivateKey.generate();
        PublicKey newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var txId = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(newKey)
            .setInitialBalance(new Hbar(2))
            .execute(client);

        if (txId.transactionId == null) { throw new Error("Null Transaction"); }

        // This will wait for the receipt to become available
        TransactionReceipt receipt = txId.transactionId.getReceipt(client);

        AccountId newAccountId = Objects.requireNonNull(receipt.accountId);

        System.out.println("account = " + newAccountId);

        new AccountDeleteTransaction()
            // note the transaction ID has to use the ID of the account being deleted
            .setTransactionId(TransactionId.generate(newAccountId))
            .setAccountId(newAccountId)
            .setTransferAccountId(OPERATOR_ID)
            .build(client)
            .sign(newKey)
            .execute(client)
            .transactionId
            .getReceipt(client);

        new AccountInfoQuery()
            .setAccountId(newAccountId)
            .setQueryPayment(new Hbar(1))
            .execute(client);

        // note the above accountInfo will fail with ACCOUNT_DELETED due to a known issue on Hedera
    }
}
