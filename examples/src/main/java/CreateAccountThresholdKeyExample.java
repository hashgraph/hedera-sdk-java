import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class CreateAccountThresholdKeyExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    private CreateAccountThresholdKeyExample() {
    }

    public static void main(String[] args) throws HederaPreCheckStatusException, TimeoutException, HederaReceiptStatusException {
        // Generate three new Ed25519 private, public key pairs
        PrivateKey[] keys = new PrivateKey[3];
        for (int i = 0; i < 3; i++) {
            keys[i] = PrivateKey.generate();
        }

        System.out.println("private keys: ");
        for (Key key : keys) {
            System.out.println(key);
        }

        // `Client.forMainnet()` is provided for connecting to Hedera mainnet
        Client client = Client.forTestnet();

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // require 2 of the 3 keys we generated to sign on anything modifying this account
        KeyList transactionKey = KeyList.withThreshold(2);
        Collections.addAll(transactionKey, keys);

        TransactionResponse transactionResponse = new AccountCreateTransaction()
            .setKey(transactionKey)
            .setInitialBalance(new Hbar(10))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = transactionResponse.getReceipt(client);

        AccountId newAccountId = Objects.requireNonNull(receipt.accountId);

        System.out.println("account = " + newAccountId);

        TransactionResponse transferTransactionResponse = new TransferTransaction()
            .addHbarTransfer(newAccountId, new Hbar(10).negated())
            .addHbarTransfer(new AccountId(3), new Hbar(10))
            // To manually sign, you must explicitly build the Transaction
            .freezeWith(client)
            // we sign with 2 of the 3 keys
            .sign(keys[0])
            .sign(keys[1])
            .execute(client);


        // (important!) wait for the transfer to go to consensus
        transferTransactionResponse.getReceipt(client);

        Hbar balanceAfter = new AccountBalanceQuery()
            .setAccountId(newAccountId)
            .execute(client)
            .hbars;

        System.out.println("account balance after transfer: " + balanceAfter);
    }
}
