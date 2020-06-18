import com.hedera.hashgraph.sdk.TransactionId;
import com.hedera.hashgraph.sdk.account.AccountId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TransactionIdTest {
    @Test
    @DisplayName("strictly increasing validStart")
    void strictlyIncreasingValidStart() {
        final AccountId id = new AccountId(2);
        Instant lastTime = new TransactionId(id).validStart;
        for (int i = 0; i < 200; ++i) {
            final Instant thisTime = new TransactionId(id).validStart;
            assertTrue(thisTime.isAfter(lastTime));
            lastTime = thisTime;
        }
    }
}
