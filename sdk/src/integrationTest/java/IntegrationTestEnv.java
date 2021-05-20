import com.hedera.hashgraph.sdk.*;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IntegrationTestEnv {
    public Client client;
    public PrivateKey operatorKey;
    public AccountId operatorId;
    public List<AccountId> nodeAccountIds;

    public static Random random = new Random();

    public IntegrationTestEnv() throws PrecheckStatusException, TimeoutException, ReceiptStatusException {
        if (System.getProperty("HEDERA_NETWORK").equals("previewnet")) {
            client = Client.forPreviewnet();
        } else if (System.getProperty("HEDERA_NETWORK").equals("localhost")) {
            var network = new Hashtable<String, AccountId>();
            network.put("127.0.0.1:50213", new AccountId(3));
//            network.put("127.0.0.1:50214", new AccountId(4));
//            network.put("127.0.0.1:50215", new AccountId(5));

            client = Client.forNetwork(network);
        } else {
            try {
                client = Client.fromConfigFile(System.getProperty("CONFIG_FILE"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
                client = Client.forTestnet();
            }
        }

        try {
            operatorKey = PrivateKey.fromString(System.getProperty("OPERATOR_KEY"));
            operatorId = AccountId.fromString(System.getProperty("OPERATOR_ID"));

            client.setOperator(operatorId, operatorKey);
        } catch (Exception e) {
        }

        assertNotNull(client.getOperatorAccountId());
        assertNotNull(client.getOperatorPublicKey());

        var key = PrivateKey.generate();

        var response = new AccountCreateTransaction()
            .setInitialBalance(new Hbar(130))
            .setKey(key)
            .execute(client);

        operatorId = Objects.requireNonNull(response.getReceipt(client).accountId);
        operatorKey = key;
        nodeAccountIds = Collections.singletonList(response.nodeId);
        client.setOperator(operatorId, operatorKey);
    }
}
