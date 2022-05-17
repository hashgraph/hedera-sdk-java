import com.hedera.hashgraph.sdk.AccountCreateTransaction;
import com.hedera.hashgraph.sdk.Hbar;
import com.hedera.hashgraph.sdk.LiveHashDeleteTransaction;
import com.hedera.hashgraph.sdk.PrecheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.Status;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LiveHashDeleteIntegrationTest {
    private static final byte[] HASH = Hex.decode("100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000002");

    @Test
    @DisplayName("Cannot delete live hash because it's not supported")
    void cannotDeleteLiveHashBecauseItsNotSupported() throws Exception {
        var testEnv = new IntegrationTestEnv(1);

        var key = PrivateKey.generateED25519();

        var response = new AccountCreateTransaction()
            .setKey(key)
            .setInitialBalance(new Hbar(1))
            .execute(testEnv.client);

        var accountId = Objects.requireNonNull(response.getReceipt(testEnv.client).accountId);

        assertThatExceptionOfType(PrecheckStatusException.class).isThrownBy(() -> {
            new LiveHashDeleteTransaction()
                .setAccountId(accountId)
                .setHash(HASH)
                .execute(testEnv.client)
                .getReceipt(testEnv.client);
        }).withMessageContaining(Status.NOT_SUPPORTED.toString());

        testEnv.close(accountId, key);
    }
}
