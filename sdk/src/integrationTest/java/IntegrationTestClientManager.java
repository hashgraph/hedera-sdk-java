import com.google.errorprone.annotations.Var;
import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IntegrationTestClientManager {
    IntegrationTestClientManager() {

    }

    public static Client getClient() {
        @Var Client client;

        client = System.getProperty("HEDERA_NETWORK").equals("previewnet") ? Client.forPreviewnet() : Client.forTestnet();

        try {
            client = Client.fromJsonFile(System.getProperty("CONFIG_FILE"));
        } catch (Exception e) {
        }

        try {
            var operatorKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            var operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

            client.setOperator(operatorId, operatorKey);
        } catch (Exception e) {
        }

        assertNotNull(client.getOperatorAccountId());
        assertNotNull(client.getOperatorAccountId());

        return client;
    }
}
