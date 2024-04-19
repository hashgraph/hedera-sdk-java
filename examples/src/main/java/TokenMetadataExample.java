import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.TokenCreateTransaction;
import com.hedera.hashgraph.sdk.TokenInfoQuery;
import com.hedera.hashgraph.sdk.TokenType;
import com.hedera.hashgraph.sdk.TokenUpdateTransaction;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Arrays;
import java.util.Objects;

public class TokenMetadataExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private static final PrivateKey METADATA_KEY = PrivateKey.generateED25519();

    private static final byte[] INITIAL_TOKEN_METADATA = new byte[]{1, 1, 1, 1, 1};

    private static final byte[] UPDATED_TOKEN_METADATA = new byte[]{2, 2, 2, 2, 2};

    private Client client;

    public static void main(String[] args) throws Exception {
        TokenMetadataExample example = new TokenMetadataExample();

        example.updateMutableTokenMetadata();

        example.updateImmutableTokenMetadata();

        example.cleanUp();
    }

    private TokenMetadataExample() throws Exception {
        client = ClientHelper.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);
    }

    private void updateMutableTokenMetadata() throws Exception {
        // create a mutable fungible token with a metadata, but without a metadata key
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(INITIAL_TOKEN_METADATA)
                .setTokenType(TokenType.FUNGIBLE_COMMON) // The same flow can be executed with a TokenType.NON_FUNGIBLE_UNIQUE (i.e. HIP-765)
                .setTreasuryAccountId(OPERATOR_ID)
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setAdminKey(OPERATOR_KEY)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        System.out.println("Created a mutable token: " + tokenId);

        var tokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        // check that metadata was set correctly
        System.out.println("Mutable token's metadata after creation: " + Arrays.toString(tokenInfoAfterCreation.metadata));

        // update token's metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(UPDATED_TOKEN_METADATA)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        // check that metadata was updated correctly
        System.out.println("Mutable token's metadata after update: " + Arrays.toString(tokenInfoAfterMetadataUpdate.metadata));
    }

    private void updateImmutableTokenMetadata() throws Exception {
        // create an immutable fungible token with a metadata key and a metadata
        var tokenId = Objects.requireNonNull(
            new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setTokenMetadata(INITIAL_TOKEN_METADATA)
                .setTokenType(TokenType.FUNGIBLE_COMMON) // The same flow can be executed with a TokenType.NON_FUNGIBLE_UNIQUE (i.e. HIP-765)
                .setTreasuryAccountId(OPERATOR_ID)
                .setMetadataKey(METADATA_KEY)
                .setDecimals(3)
                .setInitialSupply(1000000)
                .freezeWith(client)
                .sign(OPERATOR_KEY)
                .execute(client)
                .getReceipt(client)
                .tokenId
        );

        System.out.println("Created an immutable token: " + tokenId);

        var tokenInfoAfterCreation = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        // check that metadata was set correctly
        System.out.println("Immutable token's metadata after creation: " + Arrays.toString(tokenInfoAfterCreation.metadata));

        // update token's metadata
        new TokenUpdateTransaction()
            .setTokenId(tokenId)
            .setTokenMetadata(UPDATED_TOKEN_METADATA)
            .freezeWith(client)
            .sign(METADATA_KEY)
            .execute(client)
            .getReceipt(client);

        var tokenInfoAfterMetadataUpdate = new TokenInfoQuery()
            .setTokenId(tokenId)
            .execute(client);

        // check that metadata was updated correctly
        System.out.println("Immutable token's metadata after update: " + Arrays.toString(tokenInfoAfterMetadataUpdate.metadata));
    }

    private void cleanUp() throws Exception {
        client.close();
    }
}
