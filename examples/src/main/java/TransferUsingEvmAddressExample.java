import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class TransferUsingEvmAddressExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private TransferUsingEvmAddressExample() {
    }

    /*
    Transfer HBAR or tokens to a Hedera account using their public-address.
    Reference: [HIP-583 Expand alias support in CryptoCreate & CryptoTransfer Transactions](https://hips.hedera.com/hip/hip-583)
    ## Example 1
        - Create an ECSDA private key
        - Extract the ECDSA public key
        - Extract the Ethereum public address
          - Add function to calculate the Ethereum Address to example in SDK
          - Ethereum account address / public-address - This is the rightmost 20 bytes of the 32 byte Keccak-256 hash of the ECDSA public key of the account. This calculation is in the manner described by the Ethereum Yellow Paper.
        - Transfer tokens using the `TransferTransaction` to the Etherum Account Address
        - The From field should be a complete account that has a public address
        - The To field should be to a public address (to create a new account)
        - Get the child receipt or child record to return the Hedera Account ID for the new account that was created
        - Get the `AccountInfo` on the new account and show it is a hollow account by not having a public key
        - This is a hollow account in this state
        - Use the hollow account as a transaction fee payer in a HAPI transaction
        - Sign the transaction with ECDSA private key
        - Get the `AccountInfo` of the account and show the account is now a complete account by returning the public key on the account
    */
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException, IOException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this account and be signed by this key
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
        System.out.println("Corresponding evm address: " + evmAddress);

        /*
         * Step 4
         * Transfer tokens using the `TransferTransaction` to the Etherum Account Address
         *    - The From field should be a complete account that has a public address
         *    - The To field should be to a public address (to create a new account)
         */
        TransferTransaction transferTx = new TransferTransaction()
            .addHbarTransfer(OPERATOR_ID, Hbar.from(10).negated())
            .addHbarTransfer(evmAddress, Hbar.from(10))
            .freezeWith(client);

        TransferTransaction transferTxSign = transferTx.sign(OPERATOR_KEY);
        TransactionResponse transferTxSubmit = transferTxSign.execute(client);

        /*
         * Step 5
         * Get the child receipt or child record to return the Hedera Account ID for the new account that was created
         */
        TransactionReceipt receipt = new TransactionReceiptQuery()
            .setTransactionId(transferTxSubmit.transactionId)
            .setIncludeChildren(true)
            .execute(client);

        AccountId newAccountId = receipt.children.get(0).accountId;
        System.out.println(newAccountId);

        /*
         * Step 6
         * Get the `AccountInfo` on the new account and show it is a hollow account by not having a public key
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("accountInfo: " + accountInfo);

        /*
         * Step 7
         * Use the hollow account as a transaction fee payer in a HAPI transaction
         */
        client.setOperator(newAccountId, privateKey);
        PublicKey newPublicKey = PrivateKey.generateED25519().getPublicKey();

        AccountCreateTransaction transaction = new AccountCreateTransaction()
            .setKey(newPublicKey)
            .freezeWith(client);

        /*
         * Step 8
         * Sign the transaction with ECDSA private key
         */
        AccountCreateTransaction transactionSign = transaction.sign(privateKey);
        TransactionResponse transactionSubmit = transactionSign.execute(client);
        TransactionReceipt status = transactionSubmit.getReceipt(client);
        System.out.println(status);

        /*
         * Step 9
         * Get the `AccountInfo` of the account and show the account is now a complete account by returning the public key on the account
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        System.out.println("The public key of the newly created and now complete account: " + accountInfo2.key);
    }
}
