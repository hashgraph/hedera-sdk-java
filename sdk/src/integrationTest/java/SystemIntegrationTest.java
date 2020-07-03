import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.ContractId;
import com.hedera.hashgraph.sdk.FileId;
import com.hedera.hashgraph.sdk.HederaPreCheckStatusException;
import com.hedera.hashgraph.sdk.PrivateKey;
import com.hedera.hashgraph.sdk.SystemDeleteTransaction;
import com.hedera.hashgraph.sdk.SystemUndeleteTransaction;
import org.junit.jupiter.api.Test;
import org.threeten.bp.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SystemIntegrationTest {
    @Test
    void system() {
        assertDoesNotThrow(() -> {
            var operatorKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            var operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

            var client = Client.forTestnet()
                .setOperator(operatorId, operatorKey);

            assertThrows(HederaPreCheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setContractId(new ContractId(10))
                    .setExpirationTime(Instant.now())
                    .execute(client);
            });

            assertThrows(HederaPreCheckStatusException.class, () -> {
                new SystemDeleteTransaction()
                    .setFileId(new FileId(10))
                    .setExpirationTime(Instant.now())
                    .execute(client);
            });

            assertThrows(HederaPreCheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setContractId(new ContractId(10))
                    .execute(client);
            });

            assertThrows(HederaPreCheckStatusException.class, () -> {
                new SystemUndeleteTransaction()
                    .setFileId(new FileId(10))
                    .execute(client);
            });

            client.close();
        });
    }
}
