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
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.concurrent.TimeoutException;

public class AccountCreateEvmAddressExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private AccountCreateEvmAddressExample() {
    }

    /*
    Create an account and set an EVM address using the `AccountCreateTransaction`
    Reference: [HIP-583 Expand alias support in CryptoCreate & CryptoTransfer Transactions](https://hips.hedera.com/hip/hip-583)
    ## Example 1
    - Create an ECSDA private key
    - Extract the ECDSA public key
    - Extract the Ethereum public address
    - Add function in the SDK to calculate the Ethereum Address
    - Ethereum account address / public-address - This is the rightmost 20 bytes of the 32 byte Keccak-256 hash of the ECDSA public key of the account. This calculation is in the manner described by the Ethereum Yellow Paper.
    - Use the `AccountCreateTransaction` and set the EVM address field to the Ethereum public address
    - Sign the transaction with the key that us paying for the transaction
    - Get the account ID from the receipt
    - Get the `AccountInfo` and return the account details
    - Verify the evm address provided for the account matches what is in the mirror node
    */
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException, IOException {
//        Client client = Client.forName(HEDERA_NETWORK);

        Client client = Client.forNetwork(Collections.singletonMap("127.0.0.1:50211", AccountId.fromString("0.0.3"))).setMirrorNetwork(List.of("127.0.0.1:5600"));
        client.setOperator(AccountId.fromString("0.0.2"), PrivateKey.fromString("302e020100300506032b65700422042091132178e72057a1d7528025956fe39b0b847f200ab59b2fdd367017f3087137"));

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         * Step 1
         * Create an ECSDA private key
         */
        PrivateKey privateKey = PrivateKeyECDSA.generateECDSA();

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
            .setEvmAddress(evmAddress)
            .setInitialBalance(new Hbar(10));

        /*
         * Step 5
         * Sign the transaction with the key that is paying for the transaction
         */
        TransactionResponse response = accountCreateTransaction.execute(client);

        /*
         * Step 6
         * Get the account ID from the receipt
         */
        AccountId newAccountId = response.getReceipt(client).accountId;
        System.out.println(newAccountId);

        /*
         * Step 7
         * Get the `AccountInfo` and return the account details
         */
        AccountInfo info = new AccountInfoQuery()
            .setAccountId(newAccountId)
            .execute(client);


        if (info.contractAccountId.equals(evmAddress.toString())) {
            System.out.println("The addresses match");
        } else {
            System.out.println("The addresses don't match");
        }

        /*
         * Step 8
         * Verify the evm address provided for the account matches what is in the mirror node
         */
        Thread.sleep(5000);
        URL url = new URL("http://127.0.0.1:5551/api/v1/accounts?account.id=" + newAccountId);
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
                .getAsString();

            if (mirrorNodeEvmAddress.equals("0x" + evmAddress)) {
                System.out.println("The addresses match");
            } else {
                System.out.println("The addresses don't match");
            }
        }
    }
}
