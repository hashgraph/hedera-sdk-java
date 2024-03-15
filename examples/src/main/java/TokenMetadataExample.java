import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Objects;

public class TokenMetadataExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    public TokenMetadataExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        var initialTokenMetadata = new byte[]{1, 1, 1, 1, 1};
        var updatedTokenMetadata = new byte[]{2, 2, 2, 2, 2};
        var metadataKey = PrivateKey.generateED25519();

        // create a fungible token with metadata and metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(initialTokenMetadata)
                .setTokenType(TokenType.FUNGIBLE_COMMON)
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setAdminKey(OPERATOR_KEY)
                .setMetadataKey(metadataKey)
                .setFreezeDefault(false)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        var tokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        // check that metadata was set correctly
        System.out.println("Token's metadata after creation= " + tokenInfoAfterCreation.metadata);

        // update token's metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(updatedTokenMetadata)
            .sign(metadataKey)
            .execute(client);

        var tokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        // check that metadata was updated correctly
        System.out.println("Token's metadata after update= " + tokenInfoAfterMetadataUpdate.metadata);

        client.close();
    }
}
