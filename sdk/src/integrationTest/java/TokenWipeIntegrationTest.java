import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenWipeIntegrationTest {
    private static final AccountId OPERATOR_ID = AccountId.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_ID")));
    private static final PrivateKey OPERATOR_KEY = PrivateKey.fromString(Objects.requireNonNull(System.getProperty("OPERATOR_KEY")));
    private static final String CONFIG_FILE = Objects.requireNonNull(System.getProperty("CONFIG_FILE"));

    @Test
    void test() {
        assertDoesNotThrow(() -> {
            try (Client client = Client.fromJsonFile(CONFIG_FILE)) {
                // Defaults the operator account ID and key such that all generated transactions will be paid for
                // by this account and be signed by this key
                client.setOperator(OPERATOR_ID, OPERATOR_KEY);

                PrivateKey key = PrivateKey.generate();

                TransactionResponse response = new AccountCreateTransaction()
                    .setKey(key)
                    .setMaxTransactionFee(new Hbar(2))
                    .setInitialBalance(new Hbar(1))
                    .execute(client);

                AccountId accountId = response.getReceipt(client).accountId;

                response = new TokenCreateTransaction()
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

                new TokenAssociateTransaction()
                    .setNodeAccountId(response.nodeId)
                    .setAccountId(accountId)
                    .setTokenIds(tokenId)
                    .freezeWith(client)
                    .sign(OPERATOR_KEY)
                    .sign(key)
                    .execute(client)
                    .getReceipt(client);

                new TokenGrantKycTransaction()
                    .setNodeAccountId(response.nodeId)
                    .setAccountId(accountId)
                    .setTokenId(tokenId)
                    .execute(client)
                    .getReceipt(client);

                new TokenTransferTransaction()
                    .setNodeAccountId(response.nodeId)
                    .addSender(tokenId, OPERATOR_ID, 10)
                    .addRecipient(tokenId, accountId, 10)
                    .execute(client)
                    .getReceipt(client);

                new TokenWipeTransaction()
                    .setNodeAccountId(response.nodeId)
                    .setTokenId(tokenId)
                    .setAccountId(accountId)
                    .setAmount(10)
                    .execute(client)
                    .getReceipt(client);

                new AccountDeleteTransaction()
                    .setAccountId(accountId)
                    .setTransferAccountId(OPERATOR_ID)
                    .freezeWith(client)
                    .sign(OPERATOR_KEY)
                    .sign(key)
                    .execute(client)
                    .getReceipt(client);
            }
        });
    }
}
