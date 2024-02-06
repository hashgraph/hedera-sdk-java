import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class AutoCreateAccountTransferTransactionExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private AutoCreateAccountTransferTransactionExample() {
    }

    /*
    Auto-create a new account using a public-address via a `TransferTransaction`.
    Reference: [HIP-583 Expand alias support in CryptoCreate & CryptoTransfer Transactions](https://hips.hedera.com/hip/hip-583)
    ## Example 2
    - Create an ECSDA private key
    - Extract the ECDSA public key
    - Extract the Ethereum public address
    - Use the `TransferTransaction`
       - Populate the `FromAddress` with the sender Hedera AccountID
       - Populate the `ToAddress` with Ethereum public address
       - Note: Can transfer from public address to public address in the `TransferTransaction` for complete accounts. Transfers from hollow accounts will not work because the hollow account does not have a public key assigned to authorize transfers out of the account
    - Sign the `TransferTransaction` transaction using an existing Hedera account and key paying for the transaction fee
    - The `AccountCreateTransaction` is executed as a child transaction triggered by the `TransferTransaction`
    - The Hedera Account that was created has a public address the user specified in the TransferTransaction ToAddress
           - Will not have a public key at this stage
           - Cannot do anything besides receive tokens or hbars
           - The alias property of the account does not have the public address
           - Referred to as a hollow account
    - To get the new account ID ask for the child receipts or child records for the parent transaction ID of the `TransferTransaction`
    - Get the `AccountInfo` and verify the account is a hollow account with the supplied public address (may need to verify with mirror node API)
    - To enhance the hollow account to have a public key the hollow account needs to be specified as a transaction fee payer in a HAPI transaction
    - Create a HAPI transaction and assign the new hollow account as the transaction fee payer
    - Sign with the private key that corresponds to the public key on the hollow account
    - Get the `AccountInfo` for the account and return the public key on the account to show it is a complete account
    */
    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

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
         * Use the `TransferTransaction` and set the EVM address field to the Ethereum public address
         */
        TransferTransaction transferTransaction = new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, Hbar.from(10).negated())
            .addHbarTransfer(AccountId.fromEvmAddress(evmAddress), Hbar.from(10))
            .freezeWith(client);

        /*
         * Step 5
         * Sign the `TransferTransaction` transaction using an existing Hedera account and key paying for the transaction fee
         */
        TransactionResponse response = transferTransaction.execute(client);

        /*
         * Step 6
         * To get the new account ID ask for the child receipts or child records for the parent transaction ID of the `TransferTransaction`
         *     - The `AccountCreateTransaction` is executed as a child transaction triggered by the `TransferTransaction`
         */
        TransactionReceipt receipt = new TransactionReceiptQuery()
            .setTransactionId(response.transactionId)
            .setIncludeChildren(true)
            .execute(client);

        AccountId newAccountId = receipt.children.get(0).accountId;
        System.out.println(newAccountId);

        /*
         * Step 7
         * Get the `AccountInfo` and verify the account is a hollow account with the supplied public address (may need to verify with mirror node API)
         * The Hedera Account that was created has a public address the user specified in the TransferTransaction ToAddress
             - Will not have a public key at this stage
             - Cannot do anything besides receive tokens or hbars
             - The alias property of the account does not have the public address
             - Referred to as a hollow account
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        if (((KeyList) accountInfo.key).isEmpty()) {
            System.out.println("The newly created account is a hollow account");
        } else {
            throw new Exception("Not a hollow account");
        }

        /*
         * Step 8
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
         * Step 9
         * Get the `AccountInfo` for the account and return the public key on the account to show it is a complete account
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The public key of the newly created and now complete account: " + accountInfo2.key);
    }
}
