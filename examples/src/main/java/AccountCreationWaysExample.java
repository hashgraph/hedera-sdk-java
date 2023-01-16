import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public class AccountCreationWaysExample {
    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private AccountCreationWaysExample() {
    }

    /*
    Reference: [HIP-583 Expand alias support in CryptoCreate & CryptoTransfer Transactions](https://hips.hedera.com/hip/hip-583)
    ## In Hedera we have the concept of 4 different account representations
        - an account can have an account ID in shard.realm.accountNumber format (0.0.10)
        - an account can have a public key alias in 0.0.CIQNOWUYAGBLCCVX2VF75U6JMQDTUDXBOLZ5VJRDEWXQEGTI64DVCGQ format
        - an account can have an AccountId that is represented in 0x000000000000000000000000000000000000000a (for account ID 0.0.10) long zero format
        - an account can be represented by an Ethereum public address 0xb794f5ea0ba39494ce839613fffba74279579268
    */
    public static void main(String[] args) throws PrecheckStatusException, TimeoutException, ReceiptStatusException, InterruptedException, IOException {
        //Client client = Client.forName(HEDERA_NETWORK);
        Client client = Client.forNetwork(Collections.singletonMap("127.0.0.1:50211", AccountId.fromString("0.0.3"))).setMirrorNetwork(List.of("127.0.0.1:5600"));

        // Defaults the operator account ID and key such that all generated transactions will be paid for by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        /*
         *  Account ID    -   shard.realm.number format, i.e. `0.0.10` with the corresponding `0x000000000000000000000000000000000000000a` ethereum address
         */
        AccountId hederaFormat = AccountId.fromString("0.0.10");
        System.out.println("Account ID: " + hederaFormat);
        System.out.println("Account 0.0.10 corresponding Long-Zero address: " + hederaFormat.toSolidityAddress());

        /*
         *  Hedera Long-Form Account ID    -   0.0.aliasPublicKey, i.e. `0.0.CIQNOWUYAGBLCCVX2VF75U6JMQDTUDXBOLZ5VJRDEWXQEGTI64DVCGQ`
         */
        PrivateKey privateKey = PrivateKey.generateECDSA();
        PublicKey publicKey = privateKey.getPublicKey();

        // Assuming that the target shard and realm are known.
        // For now they are virtually always 0.
        AccountId aliasAccountId = publicKey.toAccountId(0, 0);
        System.out.println("Hedera Long-Form Account ID: " + aliasAccountId.toString());

        /*
         * Hedera Account Long-Zero address    -   0x000000000000000000000000000000000000000a (for accountId 0.0.10)
         */
        AccountId longZeroAddress = AccountId.fromString("0x000000000000000000000000000000000000000a");
        System.out.println("Hedera Account Long-Zero address: " + longZeroAddress);

        /*
         * Ethereum Account Address / public-address   -   0xb794f5ea0ba39494ce839613fffba74279579268
         */
        AccountId evmAddress = AccountId.fromString("0xb794f5ea0ba39494ce839613fffba74279579268");
        System.out.println("Ethereum Account Address / public-address: " + evmAddress);
    }
}
