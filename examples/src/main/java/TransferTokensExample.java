import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

public final class TransferTokensExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String CONFIG_FILE = Objects.requireNonNull(Dotenv.load().get("CONFIG_FILE"));

    private TransferTokensExample() {
    }

    public static void main(String[] args) throws IOException, TimeoutException, HederaPreCheckStatusException, HederaReceiptStatusException {
        // Generate a Ed25519 private, public key pair
        PrivateKey newKey = PrivateKey.generate();
        PublicKey newPublicKey = newKey.getPublicKey();

        System.out.println("private key = " + newKey);
        System.out.println("public key = " + newPublicKey);

        try (Client client = Client.fromJsonFile(CONFIG_FILE)) {
            // Defaults the operator account ID and key such that all generated transactions will be paid for
            // by this account and be signed by this key
            client.setOperator(OPERATOR_ID, OPERATOR_KEY);

            TransactionResponse response = new AccountCreateTransaction()
                // The only _required_ property here is `key`
                .setKey(newKey.getPublicKey())
                .setInitialBalance(Hbar.fromTinybars(1000))
                .execute(client);

            // This will wait for the receipt to become available
            TransactionReceipt receipt = response.getReceipt(client);

            AccountId newAccountId = receipt.accountId;

            System.out.println("account = " + newAccountId);

            response = new TokenCreateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setName("ffff")
                .setSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasury(OPERATOR_ID)
                .setAdminKey(OPERATOR_KEY.getPublicKey())
                .setFreezeKey(OPERATOR_KEY.getPublicKey())
                .setWipeKey(OPERATOR_KEY.getPublicKey())
                .setKycKey(OPERATOR_KEY.getPublicKey())
                .setSupplyKey(OPERATOR_KEY.getPublicKey())
                .setFreezeDefault(false)
                .setExpirationTime(Instant.now().plus(Duration.ofDays(90)).getEpochSecond())
                .execute(client);

            TokenId tokenId = response.getReceipt(client).tokenId;
            System.out.println("token = " + tokenId);

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(newAccountId)
                .setTokenIds(tokenId)
                .freezeWith(client)
                .sign(OPERATOR_KEY)
                .sign(newKey)
                .execute(client)
                .getReceipt(client);

            System.out.println("Associated account " + newAccountId + " with token " + tokenId);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(newAccountId)
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            System.out.println("Granted KYC for account " + newAccountId + " on token " + tokenId);

            new TokenTransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addSender(tokenId, OPERATOR_ID, 10)
                .addRecipient(tokenId, newAccountId, 10)
                .execute(client)
                .getReceipt(client);

            System.out.println("Sent 10 tokens from account " + OPERATOR_ID + " to account " + newAccountId + " on token " + tokenId);

            new TokenWipeTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .setAccountId(newAccountId)
                .setAmount(10)
                .execute(client)
                .getReceipt(client);

            System.out.println("Wiped balance of account " + newAccountId);

            new TokenDeleteTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            System.out.println("Deleted token " + tokenId);

            new AccountDeleteTransaction()
                .setAccountId(newAccountId)
                .setTransferAccountId(OPERATOR_ID)
                .freezeWith(client)
                .sign(OPERATOR_KEY)
                .sign(newKey)
                .execute(client)
                .getReceipt(client);

            System.out.println("Deleted account " + newAccountId);
        }
    }
}
