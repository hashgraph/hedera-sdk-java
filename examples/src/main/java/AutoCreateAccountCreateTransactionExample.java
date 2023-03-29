import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class AutoCreateAccountCreateTransactionExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private AutoCreateAccountCreateTransactionExample() {
    }

    /*
    ## Example 1:
    - Create a ECSDA private key
    - Extract the ECDSA public key
    - Extract the Ethereum public address
    - Use the `AccountCreateTransaction` and populate `setEvmAddress(publicAddress)` field with the Ethereum public address
    - Sign the `AccountCreateTransaction` transaction using an existing Hedera account and key to pay for the transaction fee
    - The Hedera account that was created has a public address the user specified in the `AccountCreateTransaction`
           - Will not have a Hedera account public key at this stage
           - The account can only receive tokens or hbars
           - This is referred to as a hollow account
           - The alias property of the account will not have the public address
    - Get the `AccountInfo` of the account and show that it is a hollow account i.e. does not have a public key
    - To enhance the hollow account to have a public key the hollow account needs to be specified as a transaction fee payer in a HAPI transaction
    - Any HAPI transaction can be used to apply the public key to the hollow account and create a complete Hedera account
    - Use a HAPI transaction and set the hollow account as the transaction fee payer
    - Sign with the ECDSA private key that corresponds to the public address on the hollow account
    - Execute the transaction
    - Get the `AccountInfo` and show that the account is now a complete account i.e. returns a public key of the account
    */
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException, IOException {
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
         * Use the `AccountCreateTransaction` and set the EVM address field to the Ethereum public address
         */
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction()
            .setInitialBalance(Hbar.fromTinybars(100))
            .setAlias(evmAddress)
            .freezeWith(client);

        /*
         * Step 5
         * Sign the `AccountCreateTransaction` transaction using an existing Hedera account and key paying for the transaction fee
         */
        TransactionResponse response = accountCreateTransaction.execute(client);

         /*
         * Step 6
         * Get the `AccountInfo` and verify the account is a hollow account with the supplied public address (may need to verify with mirror node API)
         * The Hedera Account that was created has a public address the user specified in the TransferTransaction ToAddress
             - Will not have a public key at this stage
             - Cannot do anything besides receive tokens or hbars
             - The alias property of the account does not have the public address
             - Referred to as a hollow account
         */
        TransactionReceipt receipt = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .execute(client);

        AccountId newAccountId = receipt.accountId;
        System.out.println(newAccountId);

        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (((KeyList) accountInfo.key).isEmpty()) {
            System.out.println("The newly created account is a hollow account");
        } else {
            System.out.println("Not a hollow account");
        }

        /*
         * Step 7
         * Create a HAPI transaction and assign the new hollow account as the transaction fee payer
         *     - To enhance the hollow account to have a public key the hollow account needs to be specified as a transaction fee payer in a HAPI transaction
         */
        TransactionReceipt receipt2 = new TopicCreateTransaction()
            .setTransactionId(TransactionId.generate(newAccountId))
            .setTopicMemo("Memo")
            .freezeWith(client)
            .sign(privateKey)
            .execute(client)
            .getReceipt(client);
        System.out.println("Topic id = " + receipt2.topicId);

        /*
         *
         * Step 8
         * Get the `AccountInfo` for the account and return the public key on the account to show it is a complete account
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The public key of the newly created and now complete account: " + accountInfo2.key);
    }
}
