import com.hedera.hashgraph.sdk.*;

public class AccountCreationWaysExample {

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
    public static void main(String[] args) {
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
