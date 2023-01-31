import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class CreateAccountWithAliasExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private CreateAccountWithAliasExample() {
    }

    /*
    Create an account and set a public key alias.
    Reference: [HIP-583 Expand alias support in CryptoCreate & CryptoTransfer Transactions](https://hips.hedera.com/hip/hip-583)
    ## Example 1:
    - Create an ECDSA private key
    - Get the ECDSA public key
    - Use the `AccountCreateTransaction` and populate the `setAliasKey` field
    - Sign the `AccountCreateTransaction` using an existing Hedera account and key to pay for the transaction fee
    - Execute the transaction
    - Return the Hedera account ID from the receipt of the transaction
    - Get the `AccountInfo` using the new account ID
    - Get the `AccountInfo` using the account public key in `0.0.aliasPublicKey` format
    - Show the public key and the public key alias are the same on the account
    - Show this account has a corresponding EVM address in the mirror node
    ## Example 2:
    - Create an ED2519 private key
    - Get the ED2519 public key
    - Use the `AccountCreateTransaction` and populate the `setAliasKey` field
    - Sign the `AccountCreateTransaction` using an existing Hedera account and key to pay for the transaction fee
    - Execute the transaction
    - Return the Hedera account ID from the receipt of the transaction
    - Get the `AccountInfo` using the new account ID
    - Get the `AccountInfo` using the account public key in `0.0.aliasPublicKey` format
    - Show the public key and the public key alias are the same on the account
    */
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException, IOException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Example 1
         */
        System.out.println("Example 1");

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
         * Use the `AccountCreateTransaction` and populate the `setAliasKey` field
         */
        AccountCreateTransaction accountCreateTransaction = new AccountCreateTransaction()
            .setAliasKey(publicKey)
            .setInitialBalance(new Hbar(10));

        /*
         * Step 4
         * Execute the transaction
         */
        TransactionResponse response = accountCreateTransaction.execute(client);

        /*
         * Step 5
         * Return the Hedera account ID from the receipt of the transaction
         */
        AccountId newAccountId = response.getReceipt(client).accountId;
        System.out.println(newAccountId);

        /*
         * Step 6
         * Get the `AccountInfo` using the new account ID
         */
        AccountInfo accountInfo = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);

        /*
         * Step 7
         * Get the `AccountInfo` using the account public key in `0.0.aliasPublicKey` format
         */
        AccountId aliasPublicKey = publicKey.toAccountId(0, 0);
        AccountInfo accountInfoAlias = new AccountInfoQuery()
            .setAccountId(aliasPublicKey)
            .execute(client);

        /*
         * Step 8
         * Show the public key and the public key alias are the same on the account
         */
        if (accountInfo.key.equals(accountInfo.aliasKey)
            && accountInfo.key.equals(accountInfoAlias.key)
            && accountInfoAlias.key.equals(accountInfoAlias.aliasKey)
        ) {
            System.out.println("The public key and the public key alias are the same");
        } else {
            System.out.println("The public key and the public key alias differ");
        }

        /*
         * Step 9
         * Show this account has a corresponding EVM address in the mirror node
         */
        Thread.sleep(5000);
        String link = "https://" + HEDERA_NETWORK + ".mirrornode.hedera.com/api/v1/accounts?account.id=" + newAccountId;
        URL url = new URL(link);
        HttpURLConnection con = (HttpURLConnection)url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Accept", "application/json");
        try(BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                builder.append(responseLine.trim());
            }
            JsonObject jsonObject = JsonParser.parseString(builder.toString()).getAsJsonObject();
            String mirrorNodeEvmAddress = jsonObject
                .getAsJsonArray("accounts")
                .get(0).getAsJsonObject()
                .get("evm_address")
                .toString();

            if (mirrorNodeEvmAddress != null) {
                System.out.println("The account has a corresponding EVM address in the mirror node");
            } else {
                System.out.println("The EVM address of the account is missing in the mirror node");
            }
        }

        /*
         * Example 2
         */
        System.out.println("Example 2");

        /*
         * Step 1
         * Create an ECSDA private key
         */
        PrivateKey privateKey2 = PrivateKey.generateED25519();

        /*
         * Step 2
         * Extract the ECDSA public key
         */
        PublicKey publicKey2 = privateKey2.getPublicKey();

        /*
         * Step 3
         * Use the `AccountCreateTransaction` and populate the `setAliasKey` field
         */
        AccountCreateTransaction accountCreateTransaction2 = new AccountCreateTransaction()
            .setAliasKey(publicKey2)
            .setInitialBalance(new Hbar(10));

        /*
         * Step 4
         * Execute the transaction
         */
        TransactionResponse response2 = accountCreateTransaction2.execute(client);

        /*
         * Step 5
         * Return the Hedera account ID from the receipt of the transaction
         */
        AccountId newAccountId2 = response2.getReceipt(client).accountId;
        System.out.println(newAccountId2);

        /*
         * Step 6
         * Get the `AccountInfo` using the new account ID
         */
        AccountInfo accountInfo2 = new AccountInfoQuery()
            .setAccountId(newAccountId2)
            .execute(client);

        /*
         * Step 7
         * Get the `AccountInfo` using the account public key in `0.0.aliasPublicKey` format
         */
        AccountId aliasPublicKey2 = publicKey2.toAccountId(0, 0);
        AccountInfo accountInfoAlias2 = new AccountInfoQuery()
            .setAccountId(aliasPublicKey2)
            .execute(client);

        /*
         * Step 8
         * Show the public key and the public key alias are the same on the account
         */
        if (accountInfo2.key.equals(accountInfo2.aliasKey)
            && accountInfo2.key.equals(accountInfoAlias2.key)
            && accountInfoAlias2.key.equals(accountInfoAlias2.aliasKey)
        ) {
            System.out.println("The public key and the public key alias are the same");
        } else {
            System.out.println("The public key and the public key alias differ");
        }
    }
}
