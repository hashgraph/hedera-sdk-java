import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class CreateAccountWithAliasAndReceiverSignatureRequiredExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateAccountWithAliasAndReceiverSignatureRequiredExample() {
    }

    /*
    ## Example 1:
    - Create a ECSDA private key
    - Extract the ECDSA public key
    - Extract the Ethereum public address
    - Use the `AccountCreateTransaction` and populate `setAlias(evmAddress)` field with the Ethereum public address and the `setReceiverSignatureRequired` to true
    - Sign the `AccountCreateTransaction` transaction with both the new private key and the admin key
    - Get the `AccountInfo` and show that the account has contractAccountId
    */
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, InterruptedException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1
         * Create an ECSDA private key
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();

        /*
         * Step 2
         * Extract the ECDSA public key
         */
        PublicKey publicKey = privateKey.getPublicKey();

        /*
         * Step 3
         * Extract the Ethereum public address
         */
        EvmAddress evmAddress = publicKey.toEvmAddress();
        System.out.println(evmAddress);

        /*
         * Step 4
         * Use the `AccountCreateTransaction` and populate `setAlias(evmAddress)` field with the Ethereum public address
         * and the `setReceiverSignatureRequired` to `true`
         */
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction()
            .setReceiverSignatureRequired(true)
            .setInitialBalance(Hbar.fromTinybars(100))
            .setKey(OPERATOR_KEY)
            .setAlias(evmAddress)
            .freezeWith(client);

        /*
         * Step 5
         * Sign the `AccountCreateTransaction` transaction with both the new private key and the admin key
         */
        accountCreateTransaction.sign(privateKey);
        TransactionResponse response = accountCreateTransaction.execute(client);

        AccountId newAccountId = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(client)
            .accountId;

        System.out.println("New account ID: " + newAccountId);

         /*
         * Step 6
         * Get the `AccountInfo` and show that the account has contractAccountId
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (accountInfo.contractAccountId != null) {
            System.out.println("The new account has alias " + accountInfo.contractAccountId);
        } else {
            System.out.println("The new account doesn't have alias");
        }
    }
}
