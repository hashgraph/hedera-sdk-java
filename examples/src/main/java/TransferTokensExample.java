import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import com.hedera.hashgraph.sdk.*;
import io.github.cdimascio.dotenv.Dotenv;

public final class TransferTokensExample {

    // see `.env.sample` in the repository root for how to specify these values
    // or set environment variables with the same names
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(Dotenv.load().get("OPERATOR_KEY")));
    private static final String CONFIG_FILE = Dotenv.load().get("CONFIG_FILE");
    private static final String HEDERA_NETWORK = Dotenv.load().get("HEDERA_NETWORK");

    private TransferTokensExample() {
    }

    public static void main(String[] args) throws Exception {
        Client client;

        if (HEDERA_NETWORK != null && HEDERA_NETWORK.equals("previewnet")) {
            client = Client.forPreviewnet();
        } else {
            try {
                client = Client.fromConfigFile(CONFIG_FILE != null ? CONFIG_FILE : "");
            } catch (Exception e) {
                client = Client.forTestnet();
            }
        }

        // Defaults the operator account ID and key such that all generated transactions will be paid for
        // by this account and be signed by this key
        client.setOperator(OPERATOR_ID, OPERATOR_KEY);

        // Generate a Ed25519 private, public key pair
        PrivateKey key1 = PrivateKey.generate();
        PrivateKey key2 = PrivateKey.generate();

        System.out.println("private key = " + key1);
        System.out.println("public key = " + key1.getPublicKey());
        System.out.println("private key = " + key2);
        System.out.println("public key = " + key2.getPublicKey());

        TransactionResponse response = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(key1.getPublicKey())
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        TransactionReceipt receipt = response.getReceipt(client);

        AccountId accountId1 = Objects.requireNonNull(receipt.accountId);

        System.out.println("accountId1 = " + accountId1);

        response = new AccountCreateTransaction()
            // The only _required_ property here is `key`
            .setKey(key2.getPublicKey())
            .setInitialBalance(Hbar.fromTinybars(1000))
            .execute(client);

        // This will wait for the receipt to become available
        receipt = response.getReceipt(client);

        AccountId accountId2 = Objects.requireNonNull(receipt.accountId);

        System.out.println("accountId2 = " + accountId1);

        response = new TokenCreateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenName("ffff")
            .setTokenSymbol("F")
            .setDecimals(3)
            .setInitialSupply(1000000)
            .setTreasuryAccountId(OPERATOR_ID)
            .setAdminKey(OPERATOR_KEY.getPublicKey())
            .setFreezeKey(OPERATOR_KEY.getPublicKey())
            .setWipeKey(OPERATOR_KEY.getPublicKey())
            .setKycKey(OPERATOR_KEY.getPublicKey())
            .setSupplyKey(OPERATOR_KEY.getPublicKey())
            .setFreezeDefault(false)
            .execute(client);

        TokenId tokenId = Objects.requireNonNull(response.getReceipt(client).tokenId);
        System.out.println("token = " + tokenId);

        new TokenAssociateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId1)
            .setTokenIds(tokenId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Associated account " + accountId1 + " with token " + tokenId);

        new TokenAssociateTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId2)
            .setTokenIds(tokenId)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Associated account " + accountId2 + " with token " + tokenId);

        new TokenGrantKycTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId1)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Granted KYC for account " + accountId1 + " on token " + tokenId);

        new TokenGrantKycTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setAccountId(accountId2)
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Granted KYC for account " + accountId2 + " on token " + tokenId);

        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, OPERATOR_ID, -10)
            .addTokenTransfer(tokenId, accountId1, 10)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + OPERATOR_ID + " to account " + accountId1 + " on token " + tokenId);

        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, accountId1, -10)
            .addTokenTransfer(tokenId, accountId2, 10)
            .freezeWith(client)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + accountId1 + " to account " + accountId2 + " on token " + tokenId);

        new TransferTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .addTokenTransfer(tokenId, accountId2, -10)
            .addTokenTransfer(tokenId, accountId1, 10)
            .freezeWith(client)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Sent 10 tokens from account " + accountId2 + " to account " + accountId1 + " on token " + tokenId);

        new TokenWipeTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenId(tokenId)
            .setAccountId(accountId1)
            .setAmount(10)
            .execute(client)
            .getReceipt(client);

        System.out.println("Wiped balance of account " + accountId1);

        new TokenDeleteTransaction()
            .setNodeAccountIds(Collections.singletonList(response.nodeId))
            .setTokenId(tokenId)
            .execute(client)
            .getReceipt(client);

        System.out.println("Deleted token " + tokenId);

        new AccountDeleteTransaction()
            .setAccountId(accountId1)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key1)
            .execute(client)
            .getReceipt(client);

        System.out.println("Deleted accountId1 " + accountId1);

        new AccountDeleteTransaction()
            .setAccountId(accountId2)
            .setTransferAccountId(OPERATOR_ID)
            .freezeWith(client)
            .sign(OPERATOR_KEY)
            .sign(key2)
            .execute(client)
            .getReceipt(client);

        System.out.println("Deleted accountId2" + accountId2);
    }
}
