import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.KeyList;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.PublicKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenKeyValidation;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;

public class ChangeRemoveTokenKeys {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));

    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));

    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "previewnet");

    private ChangeRemoveTokenKeys() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Admin, Supply, Wipe keys
        var adminKey = PrivateKey.generateED25519();
        var supplyKey = PrivateKey.generateED25519();
        var wipeKey = PrivateKey.generateED25519();


        // This HIP introduces ability to remove lower-privilege keys (Wipe, KYC, Freeze, Pause, Supply, Fee Schedule, Metadata) from a Token:
        // - using an update with the empty KeyList;
        var emptyKeyList = new KeyList();
        // - updating with an “invalid” key such as an Ed25519 0x0000000000000000000000000000000000000000000000000000000000000000 public key,
        // since it is (presumably) impossible to find the 32-byte string whose SHA-512 hash begins with 32 bytes of zeros.
        var unusableKey = PublicKey.fromString("0000000000000000000000000000000000000000000000000000000000000000");

        // create a non-fungible token
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("Example NFT")
                .setTokenSymbol("ENFT")
                .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
                .setTreasuryAccountId(OPERATOR_ID)
                .setAdminKey(adminKey.getPublicKey())
                .setWipeKey(wipeKey.getPublicKey())
                .setSupplyKey(supplyKey.getPublicKey())
                .freezeWith(client)
                .sign(adminKey)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        var tokenInfoBefore = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Admin Key (before removing):" + tokenInfoBefore.adminKey);
        System.out.println("Supply Key (before removing):" + tokenInfoBefore.supplyKey);
        System.out.println("Wipe Key (before removing):" + tokenInfoBefore.wipeKey);

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setWipeKey(emptyKeyList)
            .setKeyVerificationMode(TokenKeyValidation.FULL_VALIDATION) // it is by default, but we set explicitly for illustration
            .freezeWith(client)
            .sign(adminKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterWipeKeyRemoval = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Wipe Key (after removing):" + tokenInfoAfterWipeKeyRemoval.wipeKey);

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setAdminKey(emptyKeyList)
            .setKeyVerificationMode(TokenKeyValidation.NO_VALIDATION)
            .freezeWith(client)
            .sign(adminKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterAdminKeyRemoval = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Admin Key (after removing):" + tokenInfoAfterAdminKeyRemoval.adminKey);

        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setSupplyKey(unusableKey)
            .setKeyVerificationMode(TokenKeyValidation.NO_VALIDATION)
            .freezeWith(client)
            .sign(supplyKey)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterSupplyKeyRemoval = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        System.out.println("Supply Key (after removing):" + tokenInfoAfterSupplyKeyRemoval.supplyKey);

        client.close();
    }
}
