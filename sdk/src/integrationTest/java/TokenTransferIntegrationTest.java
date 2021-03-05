import com.hedera.hashgraph.sdk.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TokenTransferIntegrationTest {
    @Test
    void test() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClientNewAccount();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());
            var operatorKey = Objects.requireNonNull(client.getOperatorPublicKey());

            PrivateKey key = PrivateKey.generate();

            TransactionResponse response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .execute(client);

            AccountId accountId = response.getReceipt(client).accountId;
            assertNotNull(accountId);

            response = new TokenCreateTransaction()
                .setTokenName("ffff")
                .setTokenSymbol("F")
                .setDecimals(3)
                .setInitialSupply(1000000)
                .setTreasuryAccountId(operatorId)
                .setAdminKey(operatorKey)
                .setFreezeKey(operatorKey)
                .setWipeKey(operatorKey)
                .setKycKey(operatorKey)
                .setSupplyKey(operatorKey)
                .setFreezeDefault(false)
                .execute(client);

            TokenId tokenId = response.getReceipt(client).tokenId;
            assertNotNull(tokenId);

            new TokenAssociateTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenIds(Collections.singletonList(tokenId))
                .freezeWith(client)
                .signWithOperator(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            new TokenGrantKycTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setAccountId(accountId)
                .setTokenId(tokenId)
                .execute(client)
                .getReceipt(client);

            new TransferTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .addTokenTransfer(tokenId, operatorId, -10)
                .addTokenTransfer(tokenId, accountId, 10)
                .execute(client)
                .getReceipt(client);

            new TokenWipeTransaction()
                .setNodeAccountIds(Collections.singletonList(response.nodeId))
                .setTokenId(tokenId)
                .setAccountId(accountId)
                .setAmount(10)
                .execute(client)
                .getReceipt(client);

            client.close();
        });
    }
}
