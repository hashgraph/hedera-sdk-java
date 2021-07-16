import com.hedera.hashgraph.sdk.*;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LiveHashDeleteIntegrationTest {
    private static final byte[] HASH = Hex.decode("100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002");

    @Test
    @DisplayName("Cannot delete live hash because it's not supported")
    void cannotDeleteLiveHashBecauseItsNotSupported() {
        assertDoesNotThrow(() -> {
            var testEnv = IntegrationTestEnv.withOneNode();

            var key = PrivateKey.generate();

            var response = new AccountCreateTransaction()
                //.setNodeAccountIds(testEnv.nodeAccountIds)
                .setKey(key)
                .setInitialBalance(new Hbar(1))
                //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                .execute(testEnv.client);

            var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

            var error = assertThrows(PrecheckStatusException.class, () -> {
                new LiveHashDeleteTransaction()
                    //.setNodeAccountIds(Collections.singletonList(new AccountId(5)))
                    .setAccountId(accountId)
                    .setHash(HASH)
                    .execute(testEnv.client)
                    .getReceipt(testEnv.client);
            });

            assertTrue(error.getMessage().contains(Status.NOT_SUPPORTED.toString()));

            testEnv.cleanUpAndClose(accountId, key);
        });
    }
}
