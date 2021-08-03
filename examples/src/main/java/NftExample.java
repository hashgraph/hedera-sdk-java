import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

public final class NftExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    // HEDERA_NETWORK defaults to testnet if not specified in dotenv
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK", "testnet");

    private NftExample() {
    }

    public static void main(String[] args) throws TimeoutException, PrecheckStatusException, ReceiptStatusException {
        Client client = Client.forName(HEDERA_NETWORK);

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        PrivateKey aliceKey = PrivateKey.generate();
        AccountId aliceId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(50))
            .setKey(aliceKey)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client)
            .accountId;

        /*
         * To mint NFTs, we must first create a type of NFT on the Hedera network to mint.
         * We specify that we're creating an NFT instead of a fungible token
         * by setting tokenType to NON_FUNGIBLE_UNIQUE
         * Newly minted NFTs of this type will go to the treasury account.
         */

        TokenId nftTokenId = new TokenCreateTransaction()
            .setTokenName("Example NFT")
            .setTokenSymbol("EX")
            .setTokenType(TokenType.NON_FUNGIBLE_UNIQUE)
            .setAdminKey(aliceKey)
            .setSupplyKey(aliceKey)
            .setTreasuryAccountId(aliceId)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client)
            .tokenId;

        System.out.println(nftTokenId.toStringWithChecksum(client));

        /*
         * When we mint fungible tokens, we specify an amount that we want to mint.
         * When we mint NFTs, we do not specify an amount, we instead specify metadatas.
         * A single NFT metadata is a byte array (byte[]) of up to 100 bytes.
         * Every NFT must have a metadata.  One NFT will be minted for every metadata you add to the mint transaction.
         * You can mint up to 10 NFTs in one mint transaction.
         */

        new TokenMintTransaction()
            .setTokenId(nftTokenId)
            .addMetadata(new byte[] {1})
            .addMetadata(new byte[] {2})
            .addMetadata(new byte[] {3})
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        /*
         * Let's query info about our NFTs.
         * The TokenNftInfoQuery will fetch a list of TokenNftInfo objects.
         * We can query by TokenId, by AccountId, or by NftId.
         *
         * Let's try querying by TokenId.
         *
         * Here's a good way to think about what we're doing: for our nftTokenId, there exists on the Hedera
         * network an ordered list of individual NFTs.  This list contains every NFT of type nftTokenId that
         * exists on the network. We can specify a range of those NFTs that we want to fetch info for.
         * For example, from index 0 (inclusive) to index 3 (exclusive).
         *
         * We happen to know in this case that we've minted 3 NFTs of this type, but in the wild this is
         * information we would have to query for, so let's do that.
         */

        TokenInfo nftInfo = new TokenInfoQuery()
            .setTokenId(nftTokenId)
            .execute(client);

        System.out.println("Token info:");
        System.out.println(nftInfo);

        long nftCount = nftInfo.totalSupply;

        List<TokenNftInfo> infosByToken = new TokenNftInfoQuery()
            .byTokenId(nftTokenId)
            .setStart(0)
            .setEnd(nftCount)
            .execute(client);

        System.out.println("NFT infos by token ID:");
        System.out.println(infosByToken);

        // TODO I guess I'm going to have to put this off until MAX_NFTS_IN_PRICE_REGIME_HAVE_BEEN_MINTED
        //  has been resolved on Hedera's end

        /*
        PrivateKey bobKey = PrivateKey.generate();
        AccountId bobId = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(50))
            .setKey(bobKey)
            .freezeWith(client)
            .sign(bobKey)
            .execute(client)
            .getReceipt(client)
            .accountId;*/

        new TokenDeleteTransaction()
            .setTokenId(nftTokenId)
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        new AccountDeleteTransaction()
            .setAccountId(aliceId)
            .setTransferAccountId(client.getOperatorAccountId())
            .freezeWith(client)
            .sign(aliceKey)
            .execute(client)
            .getReceipt(client);

        client.close();
    }
}
