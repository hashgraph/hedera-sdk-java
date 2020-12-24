import com.hedera.hashgraph.sdk.*;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Duration;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class LiveHashDeleteIntegrationTest {
    private static final byte[] HASH = Hex.decode("100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002");

    @Test
    @DisplayName("Cannot delete live hash because it's not supported")
    void cannotDeleteLiveHashBecauseItsNotSupported() {
        assertDoesNotThrow(() -> {
            var client = IntegrationTestClientManager.getClient();
            var operatorId = Objects.requireNonNull(client.getOperatorAccountId());

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(client);

            var accountId = Objects.requireNonNull(response.getReceipt(client).accountId);

            var error = assertThrows(HederaPreCheckStatusException.class, () -> {
                new LiveHashDeleteTransaction()
                    .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                    .setAccountId(accountId)
                    .setHash(HASH)
                    .execute(client)
                    .getReceipt(client);
            });

            new AccountDeleteTransaction()
                .setAccountId(accountId)
                .setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .setTransferAccountId(operatorId)
                .freezeWith(client)
                .sign(key)
                .execute(client)
                .getReceipt(client);

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            client.close();
        });
    }
}
